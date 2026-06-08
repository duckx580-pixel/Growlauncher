import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, Alert, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@/src/contexts/ThemeContext';
import { useAuth } from '@/src/contexts/AuthContext';
import * as DocumentPicker from 'expo-document-picker';
import axios from 'axios';

const BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

type LuaFile = {
  id: string;
  filename: string;
  uploaded_at: string;
  file_size: number;
};

export default function LuaManagerScreen() {
  const router = useRouter();
  const { themeColor } = useTheme();
  const { user, token } = useAuth();
  const [files, setFiles] = useState<LuaFile[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (user && token) {
      fetchFiles();
    }
  }, [user, token]);

  const fetchFiles = async () => {
    if (!token) return;
    
    try {
      setRefreshing(true);
      const response = await axios.get(`${BACKEND_URL}/api/lua/files`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setFiles(response.data);
    } catch (error: any) {
      console.error('Error fetching files:', error);
      Alert.alert('Error', 'Failed to load files');
    } finally {
      setRefreshing(false);
    }
  };

  const handleUpload = async () => {
    if (!user) {
      Alert.alert('Login Required', 'Please login to upload files');
      router.push('/auth');
      return;
    }

    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: '*/*',
        copyToCacheDirectory: true,
      });

      if (result.canceled) {
        return;
      }

      const file = result.assets[0];

      // Check if file is .lua
      if (!file.name.endsWith('.lua')) {
        Alert.alert('Invalid File', 'Only .lua files are allowed');
        return;
      }

      setLoading(true);

      // Create form data
      const formData = new FormData();
      formData.append('file', {
        uri: file.uri,
        type: file.mimeType || 'application/octet-stream',
        name: file.name,
      } as any);

      // Upload file
      const response = await axios.post(`${BACKEND_URL}/api/lua/upload`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data',
        },
      });

      Alert.alert('Success', 'File uploaded successfully');
      fetchFiles();
    } catch (error: any) {
      console.error('Upload error:', error);
      Alert.alert('Error', error.response?.data?.detail || 'Upload failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (fileId: string, filename: string) => {
    Alert.alert(
      'Delete File',
      `Are you sure you want to delete ${filename}?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await axios.delete(`${BACKEND_URL}/api/lua/files/${fileId}`, {
                headers: { Authorization: `Bearer ${token}` },
              });
              Alert.alert('Success', 'File deleted');
              fetchFiles();
            } catch (error) {
              Alert.alert('Error', 'Failed to delete file');
            }
          },
        },
      ]
    );
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  };

  const renderFile = ({ item }: { item: LuaFile }) => (
    <View style={[styles.fileItem, { borderColor: themeColor }]}>
      <View style={styles.fileIcon}>
        <Ionicons name="document-text" size={32} color={themeColor} />
      </View>
      <View style={styles.fileInfo}>
        <Text style={styles.fileName}>{item.filename}</Text>
        <Text style={styles.fileDetails}>
          {formatFileSize(item.file_size)} • {formatDate(item.uploaded_at)}
        </Text>
      </View>
      <TouchableOpacity
        style={styles.deleteButton}
        onPress={() => handleDelete(item.id, item.filename)}
      >
        <Ionicons name="trash" size={24} color="#FF4444" />
      </TouchableOpacity>
    </View>
  );

  if (!user) {
    return (
      <View style={styles.container}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
            <Ionicons name="arrow-back" size={28} color={themeColor} />
          </TouchableOpacity>
          <Text style={styles.title}>Lua Manager</Text>
        </View>
        <View style={styles.emptyContainer}>
          <Ionicons name="lock-closed" size={80} color={themeColor + '50'} />
          <Text style={styles.emptyText}>Login Required</Text>
          <Text style={styles.emptySubtext}>Please login to manage Lua files</Text>
          <TouchableOpacity
            style={[styles.loginButton, { backgroundColor: themeColor }]}
            onPress={() => router.push('/auth')}
          >
            <Text style={styles.loginButtonText}>Go to Login</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={28} color={themeColor} />
        </TouchableOpacity>
        <Text style={styles.title}>Lua Manager</Text>
      </View>

      {files.length === 0 && !refreshing ? (
        <View style={styles.emptyContainer}>
          <Ionicons name="folder-open" size={80} color={themeColor + '50'} />
          <Text style={styles.emptyText}>No Lua files</Text>
          <Text style={styles.emptySubtext}>Upload your first .lua file</Text>
        </View>
      ) : (
        <FlatList
          data={files}
          renderItem={renderFile}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContainer}
          refreshing={refreshing}
          onRefresh={fetchFiles}
        />
      )}

      {/* Floating Upload Button */}
      <TouchableOpacity
        style={[styles.uploadButton, { backgroundColor: themeColor }]}
        onPress={handleUpload}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Ionicons name="add" size={32} color="#fff" />
        )}
      </TouchableOpacity>
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
  listContainer: {
    paddingHorizontal: 20,
    paddingBottom: 100,
  },
  fileItem: {
    backgroundColor: '#1A1A2E',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
  },
  fileIcon: {
    marginRight: 12,
  },
  fileInfo: {
    flex: 1,
  },
  fileName: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
  },
  fileDetails: {
    color: '#999',
    fontSize: 12,
  },
  deleteButton: {
    padding: 8,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  emptyText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: '600',
    marginTop: 20,
  },
  emptySubtext: {
    color: '#999',
    fontSize: 14,
    marginTop: 8,
    textAlign: 'center',
  },
  loginButton: {
    paddingHorizontal: 32,
    paddingVertical: 12,
    borderRadius: 8,
    marginTop: 20,
  },
  loginButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  uploadButton: {
    position: 'absolute',
    bottom: 30,
    right: 30,
    width: 64,
    height: 64,
    borderRadius: 32,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
  },
});
