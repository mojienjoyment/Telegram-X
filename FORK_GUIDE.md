# Telegram X Fork Development Guide

## Overview

This guide will help you create a customized fork of Telegram X with a focus on using **Kotlin** for new development (unless Java is absolutely necessary). The base project has ~1079 Java files and only ~8 Kotlin files in the main source, so there's significant opportunity for Kotlin migration and modernization.

---

## Table of Contents

1. [Initial Setup](#1-initial-setup)
2. [Project Structure Analysis](#2-project-structure-analysis)
3. [Kotlin Migration Strategy](#3-kotlin-migration-strategy)
4. [Recommended Features to Implement](#4-recommended-features-to-implement)
5. [UI Improvement Ideas](#5-ui-improvement-ideas)
6. [Step-by-Step Implementation Plan](#6-step-by-step-implementation-plan)
7. [Best Practices](#7-best-practices)

---

## 1. Initial Setup

### Prerequisites

- **OS**: Linux (Ubuntu 24.04 LTS recommended) or macOS
- **RAM**: Minimum 4GB (8GB+ recommended)
- **Disk Space**: At least 5.5GB free
- **Android Studio**: Latest version with Kotlin support
- **NDK**: r27c (or version specified in project)
- **SDK**: API 35 (or latest stable)

### Clone and Setup

```bash
# Clone the repository
git clone --recursive --depth=1 --shallow-submodules https://github.com/TGX-Android/Telegram-X tgx-fork
cd tgx-fork

# Initialize Git LFS
git lfs install

# Create local.properties for development
cat > local.properties << EOF
sdk.dir=/path/to/your/android/sdk
telegram.api_id=YOUR_API_ID
telegram.api_hash=YOUR_API_HASH
EOF

# Run setup script
./scripts/setup.sh
```

### Create Your Fork Identity

1. **Change Application ID** in `local.properties`:
   ```properties
   app.id=com.yourname.telegramx
   ```

2. **Update branding** in resources:
   - `/app/src/main/res/values/strings.xml` - App name
   - `/app/src/main/res/mipmap-*` - App icons
   - `/app/src/main/res/drawable-*` - Launch screens

3. **Generate new keystore** for signing:
   ```bash
   keytool -genkey -v -keystore your-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias your-alias
   ```

---

## 2. Project Structure Analysis

### Key Directories

```
/workspace/
├── app/src/main/
│   ├── java/org/thunderdog/challegram/    # Main Java codebase (1079+ files)
│   │   ├── ui/                            # UI Controllers (Activities/Fragments)
│   │   ├── data/                          # Data models
│   │   ├── tool/                          # Utility classes
│   │   ├── component/                     # Custom UI components
│   │   ├── telegram/                      # TDLib wrappers
│   │   └── service/                       # Background services
│   │
│   ├── kotlin/tgx/                        # Kotlin modules (8 files currently)
│   │   ├── td/                            # TDLib extensions
│   │   └── app/                           # App-level utilities
│   │
│   └── res/                               # Resources (layouts, strings, themes)
│
├── buildSrc/                              # Build configuration (Kotlin DSL)
├── extension/                             # Modular extensions
├── tdlib/                                 # TDLib native bindings
└── thirdparty/                            # Third-party libraries
```

### Architecture Pattern

The app uses a **custom MVC-like architecture**:
- **Controllers**: Handle UI logic (similar to ViewModels + Fragments combined)
- **Data Layer**: TDLib objects wrapped in custom classes
- **Components**: Reusable UI widgets
- **Tools**: Static utility classes (good candidates for Kotlin conversion)

---

## 3. Kotlin Migration Strategy

### Priority Order for Conversion

#### **Phase 1: Utility Classes (Easiest)**
Start with stateless utility classes that don't depend heavily on Android framework:

```kotlin
// Example: Convert Strings.java to Strings.kt
object Strings {
    @JvmStatic
    fun any(vararg strings: String?): String {
        return strings.firstOrNull { !it.isNullOrBlank() } ?: ""
    }
    
    @JvmStatic
    fun equalsIgnorePlaceholders(s1: String?, s2: String?): Boolean {
        // Implementation
    }
}
```

**Target Files:**
- `/app/src/main/java/org/thunderdog/challegram/tool/Strings.java`
- `/app/src/main/java/org/thunderdog/challegram/tool/Format.java`
- `/app/src/main/java/org/thunderdog/challegram/util/Utils.java`

#### **Phase 2: Data Models**
Convert data holders to Kotlin data classes:

```kotlin
// Before (Java)
public class Message {
    private long id;
    private long chatId;
    // constructor, getters, setters...
}

// After (Kotlin)
data class Message(
    val id: Long,
    val chatId: Long,
    // ...
)
```

#### **Phase 3: Extension Functions**
Create Kotlin extension files for common operations:

```kotlin
// tgx/td/TdExt.kt (already exists - expand this)
fun User.getFullName(): String = "${this.firstName} ${this.lastName}".trim()

fun Message.isOutgoing(): Boolean = this.senderId is MessageSenderUser && 
    (this.senderId as MessageSenderUser).userId == TdlibController.getInstance().myUserId
```

#### **Phase 4: UI Components**
Convert custom views and components:

```kotlin
class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    // Use Kotlin property delegation, coroutines, etc.
}
```

#### **Phase 5: Controllers (Most Complex)**
Leave large UI controllers for last due to complex lifecycle management.

### Migration Tools

Use Android Studio's built-in converter:
1. Open Java file
2. `Code` → `Convert Java File to Kotlin File` (Ctrl+Alt+Shift+K)
3. Review and fix warnings
4. Ensure `@JvmStatic`, `@JvmField`, `@JvmOverloads` are used where needed for Java interop

---

## 4. Recommended Features to Implement

Based on analysis of popular Telegram forks (Plus Messenger, Nekogram, Graphene, etc.):

### **High Priority Features**

#### 4.1 Privacy & Security
- **Incognito Mode**: Hide online status selectively
- **App Lock with Biometrics**: Enhanced security with fingerprint/face unlock
- **Screenshot Prevention**: Block screenshots in sensitive chats
- **Fake Last Seen**: Show custom last seen time

```kotlin
// Example: Biometric lock utility
class BiometricLock(private val context: Context) {
    private val biometricManager = BiometricManager.from(context)
    
    fun canAuthenticate(): Boolean = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }
    
    fun authenticate(callback: BiometricPrompt.AuthenticationCallback) {
        // Implementation
    }
}
```

#### 4.2 Messaging Enhancements
- **Message Scheduler**: Schedule messages for later delivery
- **Auto-Delete Timer**: Custom auto-delete timers per chat
- **Message Translation**: Built-in translation using ML Kit or API
- **Draft Persistence**: Save drafts across devices

#### 4.3 Media & Downloads
- **Download Manager**: Advanced download queue management
- **Auto-Download Rules**: Granular control per chat type/size
- **Media Editor**: Built-in photo/video editor before sending
- **GIF Search Enhancement**: Multiple GIF provider support

#### 4.4 Chat Organization
- **Advanced Folders**: Smart folders with auto-categorization
- **Chat Backup**: Export/import individual chats
- **Archive Auto-Archive**: Rules for auto-archiving
- **Pin Limits Bypass**: Unlimited pinned chats (local feature)

#### 4.5 Notification Control
- **Per-Chat Notification Profiles**: Custom sounds, vibrations, LED
- **Notification Queue**: Batch notifications
- **Quiet Hours**: Scheduled Do Not Disturb
- **Keyword Alerts**: Get notified only for specific keywords

### **Medium Priority Features**

#### 4.6 UI Customization
- **Theme Editor**: Create custom themes with color picker
- **Icon Packs**: Support for custom app icons
- **Font Changer**: Custom fonts system-wide
- **Bubble Styles**: Different message bubble shapes

#### 4.7 Bot & Channel Features
- **Bot Menu Shortcuts**: Quick access to bot commands
- **Channel Stats**: Enhanced channel statistics
- **Auto-Forward**: Automatically forward messages from channels
- **Content Filter**: Filter spam/keywords in channels

#### 4.8 Performance & Battery
- **Data Saver Mode**: Aggressive data compression
- **Battery Optimizer**: Reduce background activity
- **Cache Manager**: Smart cache cleaning rules
- **Network Switcher**: Auto-switch between WiFi/mobile

---

## 5. UI Improvement Ideas

### 5.1 Modern Design Elements

```kotlin
// Example: Material You dynamic colors
class ThemeManager(private val context: Context) {
    fun applyDynamicColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val colorScheme = DynamicColorBuilder(context).build()
            colorScheme.applyToActivityIfAvailable(activity)
        }
    }
}
```

**Improvements:**
1. **Material You Support**: Dynamic theming based on wallpaper (Android 12+)
2. **Smooth Transitions**: Shared element transitions between screens
3. **Gesture Navigation**: Swipe gestures for navigation
4. **Haptic Feedback**: Tactile responses for actions
5. **Dark Mode Variants**: True black AMOLED mode + regular dark

### 5.2 Chat Interface Enhancements

1. **Message Actions Bar**: Floating action bar on message selection
2. **Quick Reply Buttons**: Tappable suggested replies
3. **Voice Message Waveform**: Interactive waveform seeking
4. **Read Receipts Toggle**: Per-chat read receipt control
5. **Chat Background Blur**: Blur effect on custom backgrounds

### 5.3 Navigation Improvements

1. **Bottom Navigation**: Quick switch between Chats/Contacts/Calls
2. **Drawer-less Design**: Modern tab-based navigation
3. **Search Everywhere**: Global search from any screen
4. **Recent Actions**: Quick access to recent actions
5. **Floating Action Button**: Context-aware FAB

### 5.4 Accessibility

1. **Screen Reader Optimization**: Better TalkBack support
2. **Font Scaling**: Respect system font size settings
3. **High Contrast Mode**: For visually impaired users
4. **Reduced Motion**: Option to reduce animations

---

## 6. Step-by-Step Implementation Plan

### **Week 1-2: Foundation**

#### Day 1-3: Setup & Branding
- [ ] Fork repository and change application ID
- [ ] Generate new signing keys
- [ ] Update app name, icons, colors
- [ ] Set up Firebase/Huawei push (if needed)
- [ ] Verify build process works

#### Day 4-7: Kotlin Infrastructure
- [ ] Add Kotlin stdlib dependencies (already present)
- [ ] Create package structure: `com.yourname.telegramx.kotlin.*`
- [ ] Migrate 3-5 utility classes to Kotlin
- [ ] Set up Kotlin coding standards (ktlint/detekt)

#### Day 8-14: First Feature
- [ ] Implement **Biometric App Lock** (high demand feature)
- [ ] Write in pure Kotlin
- [ ] Test on multiple devices
- [ ] Document the implementation

### **Week 3-4: Core Features**

#### Week 3: Privacy Features
- [ ] Incognito mode toggle
- [ ] Screenshot prevention
- [ ] Fake last seen (local only)
- [ ] Convert privacy-related Java classes to Kotlin

#### Week 4: Messaging Features  
- [ ] Message scheduler
- [ ] Auto-delete timer customization
- [ ] Draft persistence improvement
- [ ] Create Kotlin coroutines for async operations

### **Week 5-6: UI Modernization**

#### Week 5: Theming
- [ ] Material You dynamic colors
- [ ] Custom theme editor
- [ ] AMOLED black theme
- [ ] Convert theme-related classes to Kotlin

#### Week 6: Chat Interface
- [ ] Message bubble customization
- [ ] Chat background improvements
- [ ] Gesture controls
- [ ] Haptic feedback integration

### **Week 7-8: Polish & Release**

#### Week 7: Testing & Optimization
- [ ] Bug fixes
- [ ] Performance optimization
- [ ] Memory leak detection
- [ ] Battery usage optimization

#### Week 8: Documentation & Release
- [ ] Write user documentation
- [ ] Create changelog
- [ ] Prepare release builds
- [ ] Publish to GitHub/GitLab

---

## 7. Best Practices

### Kotlin-Specific Guidelines

1. **Null Safety**: Use `?` and `!!` judiciously
   ```kotlin
   // Good
   val userName: String? = user?.firstName
   
   // Avoid
   val userName: String = user!!.firstName!!
   ```

2. **Coroutines over AsyncTasks**:
   ```kotlin
   // Good
   lifecycleScope.launch {
       val result = withContext(Dispatchers.IO) {
           loadDataFromNetwork()
       }
       updateUI(result)
   }
   ```

3. **Extension Functions**: Create reusable extensions
   ```kotlin
   fun View.visible() { visibility = View.VISIBLE }
   fun View.gone() { visibility = View.GONE }
   fun View.invisible() { visibility = View.INVISIBLE }
   ```

4. **Sealed Classes**: For type-safe hierarchies
   ```kotlin
   sealed class MessageState {
       object Loading : MessageState()
       data class Success(val messages: List<Message>) : MessageState()
       data class Error(val message: String) : MessageState()
   }
   ```

5. **Data Classes**: For model objects
   ```kotlin
   data class User(
       val id: Long,
       val firstName: String,
       val lastName: String? = null,
       val username: String? = null
   )
   ```

### Interoperability with Java

When calling Kotlin from Java:

```kotlin
// Use @JvmStatic for companion object methods
companion object {
    @JvmStatic
    fun getInstance(): MyClass = instance
}

// Use @JvmOverloads for default parameters
fun showMessage(text: String, duration: Int = Toast.LENGTH_SHORT)

// Use @JvmField for properties without getters/setters
@JvmField
val CONSTANT = "value"
```

### Code Organization

```
app/src/main/kotlin/com/yourname/telegramx/
├── features/
│   ├── privacy/
│   │   ├── BiometricLock.kt
│   │   ├── IncognitoMode.kt
│   │   └── ScreenshotPrevention.kt
│   ├── messaging/
│   │   ├── MessageScheduler.kt
│   │   └── AutoDeleteManager.kt
│   └── theming/
│       ├── ThemeEditor.kt
│       └── DynamicColors.kt
├── extensions/
│   ├── ViewExtensions.kt
│   ├── TdLibExtensions.kt
│   └── StringExtensions.kt
├── utils/
│   ├── PreferenceManager.kt
│   └── CoroutineUtils.kt
└── ui/
    ├── components/
    └── activities/
```

### Version Control Strategy

```bash
# Feature branches
git checkout -b feature/biometric-lock
git checkout -b feature/message-scheduler
git checkout -b ui/material-you-theme

# Merge to develop
git checkout develop
git merge --no-ff feature/biometric-lock

# Release tags
git tag -a v1.0.0-beta1 -m "First beta release"
```

---

## Additional Resources

### Useful Libraries to Consider

```kotlin
// In app/build.gradle.kts
dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    
    // UI
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.15.0")
    
    // Image loading
    implementation("io.coil-kt:coil:2.7.0")
    
    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    // Dependency Injection (optional)
    implementation("io.insert-koin:koin-android:3.5.6")
}
```

### Testing

```kotlin
// Unit tests
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.14.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

// UI tests
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
```

---

## Next Steps

1. **Start with the setup** (Section 1)
2. **Choose your first feature** from Section 4 (recommend Biometric Lock)
3. **Create a feature branch** and implement it in Kotlin
4. **Test thoroughly** on multiple devices
5. **Iterate and add more features**

Would you like me to:
1. **Implement a specific feature** with full code examples?
2. **Convert specific Java files** to Kotlin?
3. **Create a detailed architecture** for a particular feature?
4. **Set up the initial Kotlin package structure**?

Let me know which direction you'd like to proceed!
