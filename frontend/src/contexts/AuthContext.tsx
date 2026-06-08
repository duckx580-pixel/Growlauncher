import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import axios from 'axios';

const BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

type User = {
  id: string;
  email: string;
  name: string;
  stars: number;
};

type AuthContextType = {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string) => Promise<void>;
  logout: () => Promise<void>;
  updateProfile: (name?: string, stars?: number) => Promise<void>;
  isLoading: boolean;
};

const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  login: async () => {},
  register: async () => {},
  logout: async () => {},
  updateProfile: async () => {},
  isLoading: true,
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadAuth().catch(err => {
      console.error('Error in loadAuth:', err);
      setIsLoading(false);
    });
  }, []);

  const loadAuth = async () => {
    try {
      const savedToken = await AsyncStorage.getItem('authToken');
      if (savedToken) {
        setToken(savedToken);
        await fetchProfile(savedToken);
      }
    } catch (error) {
      console.error('Error loading auth:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchProfile = async (authToken: string) => {
    try {
      const response = await axios.get(`${BACKEND_URL}/api/auth/profile`, {
        headers: { Authorization: `Bearer ${authToken}` },
      });
      setUser(response.data);
    } catch (error) {
      console.error('Error fetching profile:', error);
      await logout();
    }
  };

  const login = async (email: string, password: string) => {
    try {
      const response = await axios.post(`${BACKEND_URL}/api/auth/login`, {
        email,
        password,
      });
      const { access_token } = response.data;
      await AsyncStorage.setItem('authToken', access_token);
      setToken(access_token);
      await fetchProfile(access_token);
    } catch (error: any) {
      throw new Error(error.response?.data?.detail || 'Login failed');
    }
  };

  const register = async (email: string, password: string, name: string) => {
    try {
      const response = await axios.post(`${BACKEND_URL}/api/auth/register`, {
        email,
        password,
        name,
      });
      const { access_token } = response.data;
      await AsyncStorage.setItem('authToken', access_token);
      setToken(access_token);
      await fetchProfile(access_token);
    } catch (error: any) {
      throw new Error(error.response?.data?.detail || 'Registration failed');
    }
  };

  const logout = async () => {
    try {
      await AsyncStorage.removeItem('authToken');
      setToken(null);
      setUser(null);
    } catch (error) {
      console.error('Error logging out:', error);
    }
  };

  const updateProfile = async (name?: string, stars?: number) => {
    if (!token) return;
    try {
      const response = await axios.put(
        `${BACKEND_URL}/api/auth/profile`,
        { name, stars },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setUser(response.data);
    } catch (error: any) {
      throw new Error(error.response?.data?.detail || 'Update failed');
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, updateProfile, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};