# 🔐 Keystore & Telegram API Setup Guide

## Quick Answers to Your Questions

### ❓ "I don't have Java installed, can I get keystore without Java?"

**YES!** You have 3 options:

#### Option 1: GitHub Actions Generates It For You (RECOMMENDED)
GitHub Actions will automatically generate a debug keystore for you - **no Java needed on your machine!**

The workflow file already includes this:
```yaml
- name: Generate Debug Keystore
  run: |
    if [ ! -f keystore/debug.keystore ]; then
      mkdir -p keystore
      keytool -genkey -v -keystore keystore/debug.keystore \
        -alias debug -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass android -keypass android \
        -dname "CN=Android Debug,O=Android,C=US"
    fi
```

**✅ This runs on GitHub's servers which have Java installed.**

#### Option 2: Use Online Keystore Generator
Websites like these can generate keystores online:
- https://www.keytoolgenerator.com/
- Or search "online keystore generator"

Download the `.jks` or `.keystore` file and upload to GitHub Secrets.

#### Option 3: Install Java Temporarily (Optional)
If you want to generate locally:
```bash
# Ubuntu/Debian
sudo apt-get update && sudo apt-get install -y openjdk-17-jdk

# Then generate
keytool -genkey -v -keystore my-release-key.jks \
  -alias my-alias -keyalg RSA -keysize 2048 -validity 10000
```

---

### ❓ "What is Telegram API and what should I do with it?"

**Telegram API credentials** are required for your app to connect to Telegram's servers.

#### What You Need:
1. **API ID** (integer, e.g., `123456`)
2. **API Hash** (string, e.g., `abcdef1234567890abcdef1234567890`)

#### How to Get Them:

1. **Go to**: https://my.telegram.org/apps
2. **Log in** with your Telegram account (phone number + code)
3. **Click** "Create Application"
4. **Fill in**:
   - App title: Your fork name (e.g., "My Telegram X")
   - Short description: "Custom Telegram X fork"
   - Platform: Android
   - Type: Leave as default
5. **Submit** and copy your:
   - **Api ID** 
   - **Api Hash**

#### Where to Put Them:

**Option A: GitHub Secrets (Recommended for CI/CD)**
Add to your repo secrets:
- `TELEGRAM_API_ID` = your API ID
- `TELEGRAM_API_HASH` = your API Hash

**Option B: Local Development**
Create `local.properties` in project root:
```properties
telegram.api.id=123456
telegram.api.hash=abcdef1234567890abcdef1234567890
```

⚠️ **Never commit these to Git!** They're already in `.gitignore`.

---

## 📋 Complete Setup Checklist

### Step 1: Create GitHub Repository
```bash
cd /workspace
git remote add origin https://github.com/YOUR_USERNAME/your-fork-name.git
git push -u origin main
```

### Step 2: Configure GitHub Secrets

Go to: `https://github.com/YOUR_USERNAME/your-fork-name/settings/secrets/actions`

**Required Secrets:**

| Secret Name | Value | Required? |
|-------------|-------|-----------|
| `TELEGRAM_API_ID` | Your API ID from my.telegram.org | ✅ YES |
| `TELEGRAM_API_HASH` | Your API Hash from my.telegram.org | ✅ YES |
| `KEYSTORE_FILE` | Base64 encoded keystore file | Optional* |
| `KEYSTORE_PASSWORD` | Your keystore password | Optional* |
| `KEY_ALIAS` | Your key alias (e.g., "mykey") | Optional* |
| `KEY_PASSWORD` | Your key password | Optional* |

\* *If you don't provide keystore secrets, GitHub Actions will use debug keystore (fine for testing/beta).*

### Step 3: Encode Your Keystore (If You Have One)

If you have a keystore file (`release.jks` or similar):

```bash
# On Linux/Mac
base64 -w 0 release.jks > keystore_base64.txt

# Copy the content of keystore_base64.txt
```

Then paste into GitHub Secret as `KEYSTORE_FILE`.

### Step 4: Test Build Locally (Optional)

```bash
# Build debug APK (no keystore needed)
./gradlew assembleArm64LatestDebug

# Output: app/build/outputs/apk/arm64/latest/debug/
```

### Step 5: Trigger GitHub Build

Push any change to trigger the workflow:
```bash
git commit --allow-empty -m "Trigger first build"
git push
```

Check Actions tab: `https://github.com/YOUR_USERNAME/your-fork-name/actions`

---

## 🎯 For Your Specific Case (Android 11+, ARM64 Only)

Your configuration is already set:
- ✅ **Min SDK**: API 30 (Android 11)
- ✅ **Target SDK**: API 37 (Android 15)
- ✅ **Compile SDK**: API 37
- ✅ **Architecture**: ARM64-v8a only
- ✅ **Java Version**: 21

No changes needed! The workflows will build for modern devices only.

---

## 🚀 Quick Start Without Java

1. **Get Telegram API credentials** from https://my.telegram.org/apps
2. **Create GitHub repo** and push code
3. **Add secrets** (TELEGRAM_API_ID, TELEGRAM_API_HASH)
4. **Don't worry about keystore** - GitHub will use debug keystore automatically
5. **Push a commit** to trigger build
6. **Download APK** from Releases page

That's it! No Java installation needed on your machine. ✨

---

## 📱 Testing the APK

Once built, the APK will work on:
- ✅ Android 11 (API 30) and newer
- ✅ Devices with ARM64 processors (most modern phones 2016+)
- ❌ Will NOT work on Android 10 or older
- ❌ Will NOT work on 32-bit only devices

To check if a device is ARM64:
```bash
adb shell getprop ro.product.cpu.abi
# Should show: arm64-v8a
```

---

## 🔧 Troubleshooting

### Build fails with "Telegram API not configured"
→ Add `TELEGRAM_API_ID` and `TELEGRAM_API_HASH` to GitHub Secrets

### APK crashes on install
→ Make sure device is Android 11+ and ARM64

### Want signed release builds?
→ Generate keystore and add 4 secrets (KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)

### Need to change target SDK later?
→ Edit `/workspace/version.properties`:
  - `version.sdk_compile=XX`
  - `version.sdk_target=XX`

---

## 📞 Next Steps After Setup

1. ✅ Get Telegram API credentials
2. ✅ Set up GitHub repo with secrets
3. ✅ Trigger first automated build
4. 🎯 Start adding Kotlin features (BiometricLock, etc.)
5. 🎯 Test on Android 11+ device
6. 🎯 Release your first version!

Questions? Check the other guides:
- `GITHUB_ACTIONS_SETUP.md` - Detailed secrets configuration
- `README_ARM64.md` - ARM64-specific info
- `QUICK_START_ARM64.md` - Quick start guide
