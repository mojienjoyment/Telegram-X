# GitHub Actions Setup Guide for ARM64 Telegram X Fork

## ✅ Configuration Complete

Your project is now configured for:
- **Min SDK**: Android 11 (API 30)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: ARM64-v8a only
- **Java Version**: Java 21

## 🚀 GitHub Actions Workflow Files Created

### 1. Main Build & Release Workflow
**File**: `.github/workflows/build-release.yml`

This workflow will:
- Build on every push to `main` branch
- Create release APK automatically
- Upload artifacts for download
- Support manual trigger from GitHub UI

### 2. Weekly Beta Builds
**File**: `.github/workflows/weekly-beta.yml`

Automatically builds every Monday with latest changes.

---

## 🔐 Required Secrets Setup

You need to configure these secrets in your GitHub repository:

### Step 1: Go to Repository Settings
1. Open your GitHub repository
2. Click **Settings** tab
3. Click **Secrets and variables** → **Actions**
4. Click **New repository secret**

### Step 2: Add Required Secrets

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `KEYSTORE_BASE64` | Your signing keystore (base64 encoded) | `UEsDBBQAAAA...` |
| `KEYSTORE_PASSWORD` | Keystore password | `your_keystore_password` |
| `KEY_ALIAS` | Key alias name | `upload` or `key0` |
| `KEY_PASSWORD` | Key password | `your_key_password` |
| `TELEGRAM_API_ID` | Telegram API ID | `12345678` |
| `TELEGRAM_API_HASH` | Telegram API Hash | `abc123def456...` |

### Step 3: Generate Keystore (if you don't have one)

```bash
keytool -genkey -v -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload -storepass YOUR_PASSWORD \
  -keypass YOUR_PASSWORD \
  -dname "CN=Telegram X Fork, OU=Development, O=YourOrg, L=City, ST=State, C=US"
```

Then encode it to base64:
```bash
# Linux/Mac
base64 -w 0 upload-keystore.jks

# Or use online tool: https://www.base64encode.org/
```

---

## 📦 Building Locally

Before pushing to GitHub, test locally:

```bash
# Clean build
./gradlew clean

# Build ARM64 release APK
./gradlew assembleArm64LatestRelease

# Build debug version for testing
./gradlew assembleArm64LatestDebug

# Output location
ls -lh app/build/outputs/apk/arm64/latest/release/
```

Expected output:
```
app-arm64-v8a-latest-release.apk (~35-40MB)
```

---

## 🎯 What Happens on GitHub

### On Every Push to Main:
1. ✅ Checkout code
2. ✅ Setup Java 21
3. ✅ Decode keystore from secrets
4. ✅ Build ARM64 release APK
5. ✅ Upload APK as artifact
6. ✅ (Optional) Create GitHub Release

### Artifacts Available:
- `app-arm64-v8a-latest-release.apk` - Signed release APK
- `mapping.txt` - ProGuard mapping (for crash debugging)
- `build-info.txt` - Build metadata

---

## 📱 Testing the Workflow

### Option 1: Manual Trigger
1. Go to **Actions** tab in your repo
2. Select **"🚀 Build & Release ARM64 APK"**
3. Click **Run workflow**
4. Wait ~6-8 minutes
5. Download APK from artifacts

### Option 2: Automatic on Push
```bash
git add .
git commit -m "test: initial ARM64 setup"
git push origin main
```

Then check **Actions** tab to see build in progress.

---

## 🔧 Customization Options

### Change App Name
Edit in `app/build.gradle`:
```gradle
android {
    defaultConfig {
        applicationId "com.yourname.telegramx"
        resValue "string", "app_name", "Your App Name"
    }
}
```

### Change Version
```bash
# Edit Config.kt
const val APP_VERSION_CODE = 1
const val APP_VERSION_NAME = "1.0.0"
```

### Enable Auto-Release
The workflow creates releases automatically when tagged:
```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## 🐛 Troubleshooting

### Build Fails with "SDK not found"
- Ensure `ANDROID_HOME` is set in workflow (already configured)
- Check if SDK platforms are installed

### Keystore Issues
- Verify base64 encoding is correct (no line breaks)
- Check passwords match exactly
- Ensure key alias is correct

### APK Too Large
- Expected size: 35-45MB for ARM64-only
- If larger, check if multiple ABIs are included

### Build Timeout
- Default timeout: 30 minutes
- Should complete in 6-8 minutes
- Increase timeout in workflow if needed

---

## 📊 Build Statistics

| Metric | Value |
|--------|-------|
| Min SDK | Android 11 (API 30) |
| Target SDK | Android 14 (API 34) |
| Architecture | ARM64-v8a only |
| Expected APK Size | ~35-40 MB |
| Build Time | 6-8 minutes |
| Java Version | 21 |

---

## ✅ Next Steps After Setup

1. **Add Secrets** to GitHub repository
2. **Test Local Build** with `./gradlew assembleArm64LatestRelease`
3. **Push to GitHub** and trigger first workflow
4. **Download & Test** APK on Android 11+ device
5. **Start Adding Features** in Kotlin!

---

## 🆘 Need Help?

Common issues and solutions:
- Check workflow logs in GitHub Actions tab
- Verify all secrets are correctly set
- Ensure keystore is valid and not corrupted
- Test locally before pushing to GitHub

