import { Stack } from 'expo-router';

export default function RootLayout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="index" />
      <Stack.Screen name="test" />
      <Stack.Screen name="launch/index" />
      <Stack.Screen name="script-hub/index" />
      <Stack.Screen name="auth/index" />
      <Stack.Screen name="lua-manager/index" />
      <Stack.Screen name="sound/index" />
      <Stack.Screen name="theme/index" />
    </Stack>
  );
}