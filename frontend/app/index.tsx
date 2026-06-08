import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Modal } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@/src/contexts/ThemeContext';

export default function Index() {
  const router = useRouter();
  const { themeColor } = useTheme();
  const [version, setVersion] = useState('v5.33');
  const [versionModalVisible, setVersionModalVisible] = useState(false);

  const versions = ['v5.48', 'v5.49', 'v5.50', 'v5.51'];

  const buttons = [
    { id: 1, icon: 'play', label: 'LAUNCH', route: '/launch' },
    { id: 2, icon: 'search', label: 'SCRIPT\nHUB', route: '/script-hub' },
    { id: 3, icon: 'settings', label: 'SETTING', route: '/auth' },
    { id: 4, icon: 'folder-open', label: 'LUA\nMANAGER', route: '/lua-manager' },
    { id: 5, icon: 'musical-notes', label: 'SOUND', route: '/sound' },
    { id: 6, icon: 'brush', label: 'THEME', route: '/theme' },
  ];

  const handleVersionSelect = (selectedVersion: string) => {
    setVersion(selectedVersion);
    setVersionModalVisible(false);
  };

  return (
    <View style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>GrowLauncher</Text>
          <Text style={styles.version}>{version}</Text>
          <TouchableOpacity
            style={[styles.switchButton, { backgroundColor: themeColor }]}
            onPress={() => setVersionModalVisible(true)}
          >
            <Ionicons name="swap-horizontal" size={20} color="#fff" />
            <Text style={styles.switchButtonText}>Switch{"\n"}Version</Text>
          </TouchableOpacity>
        </View>

        {/* Main Buttons Grid */}
        <View style={styles.buttonGrid}>
          {buttons.map((button) => (
            <TouchableOpacity
              key={button.id}
              style={[styles.button, { backgroundColor: '#2A2A3C' }]}
              onPress={() => router.push(button.route as any)}
            >
              <Ionicons name={button.icon as any} size={40} color={themeColor} />
              <Text style={styles.buttonLabel}>{button.label}</Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Library Runtime */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={[styles.sectionTitle, { color: themeColor }]}>Library Runtime</Text>
            <View style={[styles.statusBadge, { backgroundColor: themeColor }]}>
              <Text style={styles.statusText}>Installed</Text>
            </View>
          </View>
          <Text style={styles.sectionDescription}>
            Runtime powerkuy library. now you can{"\n"}directly download patches from here
          </Text>
        </View>

        {/* Crash Log */}
        <View style={styles.section}>
          <View style={styles.crashLogHeader}>
            <View style={{ flex: 1 }}>
              <Text style={[styles.sectionTitle, { color: themeColor }]}>Crash Log</Text>
              <Text style={styles.sectionDescription}>
                Because growlauncher is still{"\n"}beta that contains crash issue,{"\n"}you can open this crash log{"\n"}and share it to powerkuy to{"\n"}get fixed
              </Text>
            </View>
            <TouchableOpacity style={[styles.crashLogButton, { backgroundColor: themeColor }]}>
              <Ionicons name="document-text" size={40} color="#000" />
            </TouchableOpacity>
          </View>
        </View>

        {/* Growtopia Info */}
        <View style={styles.section}>
          <Text style={[styles.sectionTitle, { color: themeColor }]}>Growtopia</Text>
          <Text style={styles.infoText}>Current Growtopia is 64 Bit</Text>
          <Text style={styles.infoText}>Library Mod Menu Aarch 64 Bit</Text>
          <Text style={styles.infoText}>Application arm64-v8a</Text>
          <Text style={styles.infoText}>Currently using built-in growtopia</Text>
        </View>
      </ScrollView>

      {/* Version Selector Modal */}
      <Modal
        visible={versionModalVisible}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setVersionModalVisible(false)}
      >
        <TouchableOpacity
          style={styles.modalOverlay}
          activeOpacity={1}
          onPress={() => setVersionModalVisible(false)}
        >
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Select Version</Text>
            {versions.map((v) => (
              <TouchableOpacity
                key={v}
                style={[styles.versionOption, { borderColor: themeColor }]}
                onPress={() => handleVersionSelect(v)}
              >
                <Text style={[styles.versionText, { color: themeColor }]}>{v}</Text>
                {version === v && <Ionicons name="checkmark" size={24} color={themeColor} />}
              </TouchableOpacity>
            ))}
          </View>
        </TouchableOpacity>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  scrollView: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 50,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 30,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
  },
  version: {
    fontSize: 24,
    color: '#fff',
    fontWeight: '600',
  },
  switchButton: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 20,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  switchButtonText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
    textAlign: 'center',
  },
  buttonGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 30,
  },
  button: {
    width: '48%',
    aspectRatio: 1.5,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 15,
  },
  buttonLabel: {
    color: '#fff',
    fontSize: 14,
    fontWeight: 'bold',
    marginTop: 8,
    textAlign: 'center',
  },
  section: {
    backgroundColor: '#1A1A2E',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
  },
  statusText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  sectionDescription: {
    color: '#999',
    fontSize: 12,
    lineHeight: 18,
  },
  crashLogHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  crashLogButton: {
    width: 80,
    height: 80,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  infoText: {
    color: '#999',
    fontSize: 12,
    marginBottom: 4,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: '#1A1A2E',
    borderRadius: 16,
    padding: 24,
    width: '80%',
  },
  modalTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 20,
    textAlign: 'center',
  },
  versionOption: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 20,
    borderWidth: 2,
    borderRadius: 12,
    marginBottom: 12,
  },
  versionText: {
    fontSize: 18,
    fontWeight: '600',
  },
});