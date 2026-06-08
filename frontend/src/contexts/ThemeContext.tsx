import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

type ThemeContextType = {
  themeColor: string;
  setThemeColor: (color: string) => void;
};

const ThemeContext = createContext<ThemeContextType>({
  themeColor: '#7B68EE',
  setThemeColor: () => {},
});

export const useTheme = () => useContext(ThemeContext);

export const ThemeProvider = ({ children }: { children: ReactNode }) => {
  const [themeColor, setThemeColorState] = useState('#7B68EE');

  useEffect(() => {
    loadTheme();
  }, []);

  const loadTheme = async () => {
    try {
      const savedColor = await AsyncStorage.getItem('themeColor');
      if (savedColor) {
        setThemeColorState(savedColor);
      }
    } catch (error) {
      console.error('Error loading theme:', error);
    }
  };

  const setThemeColor = async (color: string) => {
    try {
      await AsyncStorage.setItem('themeColor', color);
      setThemeColorState(color);
    } catch (error) {
      console.error('Error saving theme:', error);
    }
  };

  return (
    <ThemeContext.Provider value={{ themeColor, setThemeColor }}>
      {children}
    </ThemeContext.Provider>
  );
};