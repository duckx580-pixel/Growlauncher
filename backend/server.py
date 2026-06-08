from fastapi import FastAPI, APIRouter, HTTPException, Depends, File, UploadFile
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from starlette.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
import os
import logging
from pathlib import Path
from pydantic import BaseModel, Field, EmailStr
from typing import List, Optional
import uuid
from datetime import datetime, timedelta
import bcrypt
import jwt
import base64

ROOT_DIR = Path(__file__).parent
load_dotenv(ROOT_DIR / '.env')

# MongoDB connection
mongo_url = os.environ['MONGO_URL']
client = AsyncIOMotorClient(mongo_url)
db = client[os.environ['DB_NAME']]

# JWT Configuration
SECRET_KEY = os.environ.get('JWT_SECRET', 'your-secret-key-change-this')
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30 * 24 * 60  # 30 days

# Create the main app without a prefix
app = FastAPI()

# Create a router with the /api prefix
api_router = APIRouter(prefix="/api")

# Security
security = HTTPBearer()

# ==================== MODELS ====================

class UserRegister(BaseModel):
    email: EmailStr
    password: str
    name: str

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class UserProfile(BaseModel):
    id: str
    email: str
    name: str
    stars: float = 4.0

class UpdateProfile(BaseModel):
    name: Optional[str] = None
    stars: Optional[float] = None

class LuaFileResponse(BaseModel):
    id: str
    filename: str
    uploaded_at: datetime
    file_size: int

class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"

# ==================== HELPER FUNCTIONS ====================

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))

async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    try:
        token = credentials.credentials
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = payload.get("sub")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Invalid authentication credentials")
        
        user = await db.users.find_one({"id": user_id})
        if user is None:
            raise HTTPException(status_code=401, detail="User not found")
        return user
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token has expired")
    except jwt.JWTError:
        raise HTTPException(status_code=401, detail="Could not validate credentials")

# ==================== AUTH ROUTES ====================

@api_router.post("/auth/register", response_model=Token)
async def register(user: UserRegister):
    # Check if user exists
    existing_user = await db.users.find_one({"email": user.email})
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    # Create user
    user_id = str(uuid.uuid4())
    hashed_pwd = hash_password(user.password)
    
    user_doc = {
        "id": user_id,
        "email": user.email,
        "password": hashed_pwd,
        "name": user.name,
        "stars": 4.0,
        "created_at": datetime.utcnow()
    }
    
    await db.users.insert_one(user_doc)
    
    # Create token
    access_token = create_access_token({"sub": user_id})
    return {"access_token": access_token}

@api_router.post("/auth/login", response_model=Token)
async def login(user: UserLogin):
    # Find user
    db_user = await db.users.find_one({"email": user.email})
    if not db_user or not verify_password(user.password, db_user["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")
    
    # Create token
    access_token = create_access_token({"sub": db_user["id"]})
    return {"access_token": access_token}

@api_router.get("/auth/profile", response_model=UserProfile)
async def get_profile(current_user: dict = Depends(get_current_user)):
    return UserProfile(
        id=current_user["id"],
        email=current_user["email"],
        name=current_user["name"],
        stars=current_user.get("stars", 4.0)
    )

@api_router.put("/auth/profile", response_model=UserProfile)
async def update_profile(update: UpdateProfile, current_user: dict = Depends(get_current_user)):
    update_data = {}
    if update.name is not None:
        update_data["name"] = update.name
    if update.stars is not None:
        # Validate stars are between 0 and 5
        if update.stars < 0 or update.stars > 5:
            raise HTTPException(status_code=400, detail="Stars must be between 0 and 5")
        update_data["stars"] = update.stars
    
    if update_data:
        await db.users.update_one(
            {"id": current_user["id"]},
            {"$set": update_data}
        )
    
    # Fetch updated user
    updated_user = await db.users.find_one({"id": current_user["id"]})
    return UserProfile(
        id=updated_user["id"],
        email=updated_user["email"],
        name=updated_user["name"],
        stars=updated_user.get("stars", 4.0)
    )

# ==================== LUA FILE ROUTES ====================

@api_router.post("/lua/upload", response_model=LuaFileResponse)
async def upload_lua_file(
    file: UploadFile = File(...),
    current_user: dict = Depends(get_current_user)
):
    # Validate file extension
    if not file.filename.endswith('.lua'):
        raise HTTPException(status_code=400, detail="Only .lua files are allowed")
    
    # Read file content
    content = await file.read()
    file_size = len(content)
    
    # Convert to base64 for storage
    content_base64 = base64.b64encode(content).decode('utf-8')
    
    # Create file document
    file_id = str(uuid.uuid4())
    file_doc = {
        "id": file_id,
        "user_id": current_user["id"],
        "filename": file.filename,
        "content": content_base64,
        "file_size": file_size,
        "uploaded_at": datetime.utcnow()
    }
    
    await db.lua_files.insert_one(file_doc)
    
    return LuaFileResponse(
        id=file_id,
        filename=file.filename,
        uploaded_at=file_doc["uploaded_at"],
        file_size=file_size
    )

@api_router.get("/lua/files", response_model=List[LuaFileResponse])
async def get_lua_files(current_user: dict = Depends(get_current_user)):
    files = await db.lua_files.find({"user_id": current_user["id"]}).to_list(1000)
    return [
        LuaFileResponse(
            id=f["id"],
            filename=f["filename"],
            uploaded_at=f["uploaded_at"],
            file_size=f["file_size"]
        )
        for f in files
    ]

@api_router.delete("/lua/files/{file_id}")
async def delete_lua_file(file_id: str, current_user: dict = Depends(get_current_user)):
    result = await db.lua_files.delete_one({"id": file_id, "user_id": current_user["id"]})
    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="File not found")
    return {"message": "File deleted successfully"}

# ==================== HEALTH CHECK ====================

@api_router.get("/")
async def root():
    return {"message": "GrowLauncher API", "version": "5.33"}

# Include the router in the main app
app.include_router(api_router)

app.add_middleware(
    CORSMiddleware,
    allow_credentials=True,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

@app.on_event("shutdown")
async def shutdown_db_client():
    client.close()
