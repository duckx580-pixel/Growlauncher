import { Stack } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { useEffect } from 'react';
import { Platform } from 'react-native';
import { useIconFonts } from '@/src/hooks/use-icon-fonts';
import { ThemeProvider } from '@/src/contexts/ThemeContext';
import { AuthProvider } from '@/src/contexts/AuthContext';

// Only prevent auto-hide on native platforms
if (Platform.OS !== 'web') {
  SplashScreen.preventAutoHideAsync();
}

export default function RootLayout() {
  const [loaded, error] = useIconFonts();

  useEffect(() => {
    if (loaded || error) {
      if (Platform.OS !== 'web') {
        SplashScreen.hideAsync();
      }
    }
  }, [loaded, error]);

  // Don't block rendering on web
  if (!loaded && !error && Platform.OS !== 'web') return null;

  return (
    <ThemeProvider>
      <AuthProvider>
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="launch/index" />
          <Stack.Screen name="script-hub/index" />
          <Stack.Screen name="auth/index" />
          <Stack.Screen name="lua-manager/index" />
          <Stack.Screen name="sound/index" />
          <Stack.Screen name="theme/index" />
        </Stack>
      </AuthProvider>
    </ThemeProvider>
  );
}