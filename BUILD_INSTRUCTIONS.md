# 🚀 GrowLauncher - Build Instructions

## How to Build APK (Android) and iOS Installation Files

### Prerequisites
1. Your code is already on GitHub ✅
2. Install Node.js on your computer
3. Create free Expo account at: expo.dev

---

## 📦 STEP 1: Setup on Your Computer

```bash
# Clone your repository from GitHub
git clone <your-github-repo-url>
cd <your-repo-name>/frontend

# Install dependencies
npm install

# Install EAS CLI globally
npm install -g eas-cli

# Login to your Expo account
eas login
```

---

## 🤖 STEP 2: Build Android APK

```bash
# Build APK file
eas build --platform android --profile preview
```

**What happens:**
- EAS builds your app on their servers (takes 10-15 minutes)
- You'll get a download link when done
- Download the .apk file

**Installation:**
- Send APK to your Android phone
- Open it and click Install
- Done! ✅

---

## 📱 STEP 3: Build iOS (TestFlight)

```bash
# Build for iOS
eas build --platform ios --profile preview
```

**What happens:**
- Builds iOS app (takes 15-20 minutes)
- Creates TestFlight-compatible build

**To distribute:**
```bash
# Submit to TestFlight (requires Apple Developer account)
eas submit --platform ios
```

**OR use Expo's internal distribution:**
- You'll get a direct installation link
- Share with users
- They can install without TestFlight!

---

## ⚡ FASTEST METHOD: Build Both at Once

```bash
# Build both Android and iOS
eas build --platform all --profile preview
```

---

## 📥 After Building - Download Links

After build completes, you'll see:

```
✅ Build complete!
📦 Android APK: https://expo.dev/artifacts/eas/...apk
📱 iOS IPA: https://expo.dev/artifacts/eas/...ipa

Install URL: https://expo.dev/accounts/yourname/projects/growlauncher/builds/...
```

**Share the Install URL** - anyone can click it and install your app!

---

## 🎯 Alternative: Use Expo Go (For Quick Testing)

Don't want to build yet? Use Expo Go:

1. Install "Expo Go" app (Android/iPhone)
2. Run: `npx expo start`
3. Scan QR code
4. App runs instantly!

---

## 💡 Important Notes

### For Android APK:
- ✅ No account needed
- ✅ Works on any Android device
- ✅ Free forever

### For iOS:
- ⚠️ Apple limits installations without developer account
- ✅ Use TestFlight for unlimited beta testing (free)
- ✅ Or use Expo's internal distribution (100 devices free)

---

## 🔗 Useful Links

- **Expo Documentation**: https://docs.expo.dev/build/introduction/
- **EAS Build**: https://expo.dev/eas
- **Your Expo Dashboard**: https://expo.dev/accounts/[your-username]/projects

---

## 🆘 Troubleshooting

**Problem:** "eas: command not found"
```bash
npm install -g eas-cli
```

**Problem:** "Not logged in"
```bash
eas login
```

**Problem:** Build fails
- Check your Expo dashboard for error details
- Make sure app.json is configured correctly (it is!)

---

## ✅ Your App is Ready to Build!

All configuration is done. Just follow the steps above to generate:
- 📦 Android APK file
- 📱 iOS installation link

**Questions?** Check Expo docs or contact support!
