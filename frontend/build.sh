#!/bin/bash

# GrowLauncher v5.49 - One-Click Build Script
# Run this on your computer to build APK and iOS

echo "🚀 Building GrowLauncher v5.49..."
echo ""

# Check if EAS CLI is installed
if ! command -v eas &> /dev/null
then
    echo "📦 Installing EAS CLI..."
    npm install -g eas-cli
fi

# Login to Expo
echo "🔐 Please login to your Expo account..."
eas login

echo ""
echo "✅ Logged in successfully!"
echo ""

# Build Android APK
echo "🤖 Building Android APK (will take 10-15 minutes)..."
eas build --platform android --profile preview --non-interactive

echo ""
echo "✅ Android APK build started!"
echo ""

# Build iOS
echo "📱 Building iOS app (will take 15-20 minutes)..."
eas build --platform ios --profile preview --non-interactive

echo ""
echo "✅ iOS build started!"
echo ""
echo "⏰ Builds are running on Expo's servers..."
echo "📧 You'll receive email notifications when complete!"
echo ""
echo "🔗 Check build status at: https://expo.dev/accounts/[your-username]/projects/growlauncher/builds"
echo ""
echo "📦 After builds complete:"
echo "   - Android APK: Download link will be provided"
echo "   - iOS: Installation link will be provided"
echo ""
echo "✨ Done! Waiting for builds to complete..."
