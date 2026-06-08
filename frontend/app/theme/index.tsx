import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@/src/contexts/ThemeContext';

const colors = [
  { name: 'Purple', value: '#7B68EE' },
  { name: 'Red', value: '#FF4444' },
  { name: 'Blue', value: '#4169E1' },
  { name: 'Yellow', value: '#FFD700' },
  { name: 'Green', value: '#32CD32' },
  { name: 'Orange', value: '#FF8C00' },
  { name: 'Pink', value: '#FF69B4' },
  { name: 'Cyan', value: '#00CED1' },
  { name: 'Lime', value: '#00FF00' },
  { name: 'Magenta', value: '#FF00FF' },
  { name: 'Teal', value: '#008080' },
  { name: 'Indigo', value: '#4B0082' },
];

export default function ThemeScreen() {
  const router = useRouter();
  const { themeColor, setThemeColor } = useTheme();

  const handleColorSelect = (color: string) => {
    setThemeColor(color);
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={28} color={themeColor} />
        </TouchableOpacity>
        <Text style={styles.title}>Theme Colors</Text>
      </View>

      {/* Color Grid */}
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        <View style={styles.colorGrid}>
          {colors.map((color) => (
            <TouchableOpacity
              key={color.value}
              style={styles.colorContainer}
              onPress={() => handleColorSelect(color.value)}
            >
              <View
                style={[
                  styles.colorBox,
                  { backgroundColor: color.value },
                  themeColor === color.value && styles.selectedColor,
                ]}
              >
                {themeColor === color.value && (
                  <Ionicons name="checkmark" size={32} color="#fff" />
                )}
              </View>
              <Text style={styles.colorName}>{color.name}</Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Preview Section */}
        <View style={styles.previewSection}>
          <Text style={styles.previewTitle}>Preview</Text>
          <View style={styles.previewBox}>
            <View style={[styles.previewButton, { backgroundColor: themeColor }]}>
              <Text style={styles.previewButtonText}>Sample Button</Text>
            </View>
            <Ionicons name="game-controller" size={48} color={themeColor} style={styles.previewIcon} />
            <Text style={[styles.previewText, { color: themeColor }]}>Themed Text</Text>
          </View>
        </View>
      </ScrollView>
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
  scrollView: {
    flex: 1,
    paddingHorizontal: 20,
  },
  colorGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  colorContainer: {
    width: '30%',
    alignItems: 'center',
    marginBottom: 20,
  },
  colorBox: {
    width: 80,
    height: 80,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  selectedColor: {
    borderWidth: 4,
    borderColor: '#fff',
  },
  colorName: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  previewSection: {
    marginTop: 20,
    marginBottom: 40,
  },
  previewTitle: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  previewBox: {
    backgroundColor: '#1A1A2E',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
  },
  previewButton: {
    paddingHorizontal: 32,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 16,
  },
  previewButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  previewIcon: {
    marginVertical: 12,
  },
  previewText: {
    fontSize: 18,
    fontWeight: '600',
    marginTop: 12,
  },
});