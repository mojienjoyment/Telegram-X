# GitHub Actions Secrets Setup Guide

## Required Secrets for Automated Builds

Go to your repository: **Settings → Secrets and variables → Actions**

### 🔐 For Signed Release Builds

#### 1. Create a Keystore (if you don't have one)

```bash
keytool -genkey -v \
  -keystore release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias telegramx_arm64
```

**Important**: Store this file securely! You'll need it for all future releases.

#### 2. Encode Keystore to Base64

**Linux/Mac:**
```bash
base64 -w 0 release-key.jks
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.jks"))
```

Copy the output string (it will be very long).

#### 3. Add Repository Secrets

Click **"New repository secret"** and add these:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `KEYSTORE_BASE64` | [Base64 string from step 2] | Your encoded keystore file |
| `KEYSTORE_PASSWORD` | your_keystore_password | Password for the keystore |
| `KEY_ALIAS` | telegramx_arm64 | The alias you used when creating keystore |
| `KEY_PASSWORD` | your_key_password | Password for the key (may be same as keystore password) |

### 📱 Optional: Telegram Channel Upload

For automatic upload to Telegram channel after builds:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `TELEGRAM_BOT_TOKEN` | 123456789:ABCdefGHIjklMNOpqrsTUVwxyz | Bot token from @BotFather |
| `TELEGRAM_CHAT_ID` | -1001234567890 | Channel/Group ID (use @RawDataBot to get it) |

#### How to Get Telegram Chat ID:

1. Create a bot via [@BotFather](https://t.me/BotFather)
2. Add bot to your channel as admin
3. Forward any message from your channel to [@RawDataBot](https://t.me/RawDataBot)
4. Copy the `chat.id` value (will be negative for channels, e.g., `-1001234567890`)

### 🔧 Optional: API Credentials

For full app functionality:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `TELEGRAM_API_ID` | 12345678 | Your API ID from my.telegram.org |
| `TELEGRAM_API_HASH` | abc123def456... | Your API hash from my.telegram.org |

**How to get Telegram API credentials:**

1. Go to https://my.telegram.org
2. Log in with your phone number
3. Click "API development tools"
4. Create a new application
5. Copy the `App api_id` and `App api_hash`

## Workflow Triggers

### Automatic Builds On:
- ✅ Push to `main` or `master` branch
- ✅ Pull requests
- ✅ Git tags starting with `v` (e.g., `v1.0.0`)
- ✅ Every Monday at 00:00 UTC (weekly beta)
- ✅ Manual trigger from Actions tab

### Manual Build:
1. Go to **Actions** tab
2. Select workflow: **"Build ARM64 APK"**
3. Click **"Run workflow"**
4. Choose branch and build type
5. Click **"Run workflow"** again

## Downloading Artifacts

### From GitHub UI:
1. Go to **Actions** tab
2. Select the workflow run
3. Scroll down to **"Artifacts"** section
4. Click on artifact name to download

### From Release Page:
If tagged release:
1. Go to **Releases** section
2. Find latest release
3. Download APK from assets

## Verifying APK Signature

After downloading, verify the APK is signed correctly:

```bash
# Using apksigner (from Android SDK)
apksigner verify --print-certs your-apk.apk

# Or using jarsigner
jarsigner -verify -verbose -certs your-apk.apk
```

## Security Best Practices

✅ **DO:**
- Keep your keystore secure and backed up
- Use strong passwords
- Enable 2FA on your GitHub account
- Rotate secrets periodically
- Use separate bots for different channels

❌ **DON'T:**
- Never commit keystore files to Git
- Never share your private keys
- Don't use weak passwords
- Don't reuse passwords across services
- Don't expose secrets in logs or issues

## Troubleshooting

### Error: "Keystore was tampered with, or password was incorrect"
- Verify `KEYSTORE_PASSWORD` secret is correct
- Ensure base64 encoding didn't corrupt the file

### Error: "Alias does not exist"
- Check `KEY_ALIAS` matches exactly what you created
- Case-sensitive!

### Error: "Signing failed: Key was not found"
- Verify `KEY_PASSWORD` is correct
- May be different from keystore password

### Build succeeds but APK is unsigned
- Check that all 4 keystore secrets are set
- Verify workflow has access to secrets (not fork PRs)

## Advanced: Multiple Flavors

If you want to build multiple variants, create additional workflows:

```yaml
# Example: build-debug.yml
- name: Build Debug APK
  run: ./gradlew assembleArm64LatestDebug
```

## Monitoring Build Status

### Email Notifications:
GitHub sends emails for:
- Failed builds on main branch
- Successful releases

### Slack/Discord Integration:
Add webhook notifications in workflow:

```yaml
- name: Notify Slack
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "Build failed: ${{ github.workflow }}"
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

---

**Need Help?**
- GitHub Actions Docs: https://docs.github.com/en/actions
- Android Signing Guide: https://developer.android.com/studio/publish/app-signing
