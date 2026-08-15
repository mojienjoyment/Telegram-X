# Quick Start Guide - Telegram X Kotlin Fork

## 🚀 Getting Started in 5 Minutes

This guide shows you how to quickly use the new Kotlin modules in your Telegram X fork.

---

## 1️⃣ Biometric App Lock

### Basic Setup

```kotlin
// In your Activity or Fragment
class MainActivity : AppCompatActivity() {
    
    private lateinit var biometricLock: BiometricLock
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        biometricLock = BiometricLock(this)
        
        // Enable biometric lock in settings
        biometricLock.setEnabled(true)
    }
    
    private fun checkAndAuthenticate() {
        if (biometricLock.needsAuthentication()) {
            biometricLock.authenticate(
                activity = this,
                title = "Unlock Telegram X",
                subtitle = "Authenticate to continue",
                description = "Use your fingerprint or device credentials",
                allowDeviceCredential = true,
                onSuccess = {
                    // Authentication successful - proceed
                    Log.i("BiometricLock", "User authenticated!")
                },
                onError = { errorCode, errorMessage ->
                    // Handle error
                    Log.e("BiometricLock", "Error: $errorCode - $errorMessage")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_CANCELED -> {
                            // User canceled - decide whether to allow
                        }
                        BiometricPrompt.ERROR_LOCKOUT -> {
                            // Too many failed attempts
                        }
                    }
                },
                onFailed = {
                    // Wrong fingerprint - try again
                    Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
```

### Check Biometric Availability

```kotlin
val biometricLock = BiometricLock(context)

when (biometricLock.getBiometricState()) {
    BiometricLock.BiometricState.AVAILABLE -> {
        // Ready to use biometrics
        showBiometricOption()
    }
    BiometricLock.BiometricState.HARDWARE_UNAVAILABLE -> {
        // No biometric sensor
        showDeviceCredentialOnly()
    }
    BiometricLock.BiometricState.NO_ENROLLED -> {
        // Sensor exists but no fingerprints enrolled
        promptToEnroll()
    }
    else -> {
        // Disabled or unknown
        hideBiometricOption()
    }
}

// Simple check
if (biometricLock.isBiometricAvailable()) {
    // Use biometrics
}
```

### From Java Code

```java
BiometricLock biometricLock = new BiometricLock(context);

if (biometricLock.isBiometricAvailable()) {
    biometricLock.setEnabled(true);
    
    biometricLock.authenticate(
        activity,
        "Unlock App",
        "Use biometric",
        null,  // description
        true,  // allow device credential
        () -> {
            // Success
        },
        (errorCode, errorMessage) -> {
            // Error
        },
        () -> {
            // Failed attempt
        }
    );
}
```

---

## 2️⃣ View Extensions

### Visibility Control

```kotlin
// Instead of this (Java style):
myView.setVisibility(View.VISIBLE)
otherView.setVisibility(View.GONE)

// Use this (Kotlin):
myView.visible()
otherView.gone()
invisibleView.invisible()

// Conditional visibility
loadingView.visibleIf(isLoading)
errorView.visibleIf(hasError)

// Toggle
toggleButton.toggleVisibility()

// Check state
if (button.isVisible()) {
    // Do something
}
```

### Click Handlers

```kotlin
// Debounced click (prevents rapid double-taps)
submitButton.clickWithDebounce(debounceTimeMs = 500L) { view ->
    submitForm()
}

// Long press then click
secretButton.longPressThenClick(longClickDuration = 1000L) { view ->
    showSecretFeature()
}

// Regular click still works
normalButton.setOnClickListener {
    doSomething()
}
```

### Animations

```kotlin
// Fade in
loadingSpinner.fadeIn(duration = 300L)

// Fade out with callback
progressBar.fadeOut(duration = 200L) {
    // Animation complete
    progressBar.gone()
}

// Pulse effect (for attention)
newMessageBadge.pulse(duration = 1000L, repeatCount = 3)

// Custom animations
customView.startAnimation(yourAnimation)
```

### Styling & Layout

```kotlin
// Set padding in DP (not pixels!)
cardView.setPaddingDp(16)  // All sides
layout.setPaddingDp(8, 16, 8, 16)  // L, T, R, B

// Remove padding
view.clearPadding()

// Enable ripple effect
button.enableRipple()

// Make clickable without action
placeholderView.makeClickable()

// Disable interaction
formView.disableInteraction()

// Enable interaction
formView.enableInteraction()

// Get dimensions after layout
myView.onGlobalLayout { width, height ->
    Log.d("Size", "$width x $height")
}

// Check if laid out
if (myView.isLaidOut()) {
    // Safe to use width/height
}
```

### Resource Access

```kotlin
// Get resources from View context
val color = view.getColor(R.color.primary)
val string = view.getString(R.string.app_name)
val formatted = view.getString(R.string.welcome, userName)
val drawable = view.getDrawable(R.drawable.ic_launcher)

// Set background from color resource
view.setBackgroundFromResource(R.color.background)
```

---

## 3️⃣ Preference Manager

### Modern Property Delegation

```kotlin
// Create a settings class
class AppSettings(context: Context) {
    private val prefs = PreferenceHelper(context)
    
    // Define preferences as properties
    var userName: String by prefs.string("user_name", "Guest")
    var isLoggedIn: Boolean by prefs.boolean("logged_in", false)
    var messageCount: Int by prefs.int("message_count", 0)
    var lastSyncTime: Long by prefs.long("last_sync", 0L)
    var fontSize: Float by prefs.float("font_size", 14.0f)
    var blockedUsers: Set<String> by prefs.stringSet("blocked_users", emptySet())
    
    // Nullable preference
    var avatarUrl: String? by prefs.nullableString("avatar_url")
}

// Usage - like normal properties!
val settings = AppSettings(context)

// Read
val name = settings.userName
val count = settings.messageCount

// Write
settings.userName = "John Doe"
settings.messageCount = 42
settings.isLoggedIn = true

// Automatically persisted!
```

### Traditional API

```kotlin
val prefs = PreferenceHelper(context)

// Save values
prefs.save("string_key", "Hello")
prefs.save("int_key", 42)
prefs.save("bool_key", true)
prefs.save("long_key", 1000L)
prefs.save("float_key", 3.14f)
prefs.save("set_key", setOf("a", "b", "c"))

// Get values
val str = prefs.getString("string_key", "default")
val num = prefs.getInt("int_key", 0)
val bool = prefs.getBoolean("bool_key", false)
val long = prefs.getLong("long_key", 0L)
val float = prefs.getFloat("float_key", 0f)
val set = prefs.getStringSet("set_key", emptySet())

// Check existence
if (prefs.contains("key")) {
    // Key exists
}

// Remove
prefs.remove("key")

// Clear all
prefs.clear()
```

### Batch Operations

```kotlin
val prefs = PreferenceHelper(context)

// Transaction for multiple writes
prefs.edit { editor ->
    editor.putString("key1", "value1")
    editor.putInt("key2", 42)
    editor.putBoolean("key3", true)
    // All applied together
}
```

### Change Listeners

```kotlin
val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
    when (key) {
        "user_name" -> updateUserName()
        "theme" -> applyTheme()
    }
}

prefs.registerOnChangeListener(listener)

// Don't forget to unregister
prefs.unregisterOnChangeListener(listener)
```

### Global Prefs Object

```kotlin
// Initialize once (in Application class)
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(applicationContext)
    }
}

// Use anywhere
var setting: String by Prefs.string("key", "default")
val value = Prefs.getBoolean("enabled", false)
Prefs.save("key", value)
```

### Context Extension

```kotlin
// Create from any Context
val prefs = context.preferences()
val namedPrefs = context.preferences("my_prefs_name")
```

---

## 4️⃣ Complete Example - Settings Screen

```kotlin
class SettingsFragment : Fragment() {
    
    // Biometric lock
    private lateinit var biometricLock: BiometricLock
    
    // App settings
    private class Settings(context: Context) {
        private val prefs = PreferenceHelper(context)
        
        var enableBiometric: Boolean by prefs.boolean("biometric_enabled", false)
        var authTimeout: Int by prefs.int("auth_timeout_minutes", 5)
        var theme: String by prefs.string("theme", "system")
        var fontSize: Int by prefs.int("font_size", 14)
    }
    
    private lateinit var settings: Settings
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        biometricLock = BiometricLock(requireContext())
        settings = Settings(requireContext())
        
        // Biometric toggle
        val biometricSwitch = view.findViewById<SwitchCompat>(R.id.biometric_switch)
        biometricSwitch.isChecked = settings.enableBiometric
        
        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && biometricLock.isBiometricAvailable()) {
                // Test authentication before enabling
                biometricLock.authenticate(
                    activity = requireActivity(),
                    title = "Verify Biometric",
                    subtitle = "Confirm your identity",
                    onSuccess = {
                        settings.enableBiometric = true
                        biometricSwitch.isChecked = true
                    },
                    onError = { _, _ ->
                        settings.enableBiometric = false
                        biometricSwitch.isChecked = false
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                settings.enableBiometric = isChecked
            }
        }
        
        // Theme selector
        val themeSpinner = view.findViewById<Spinner>(R.id.theme_spinner)
        themeSpinner.selectedItemPosition = when (settings.theme) {
            "light" -> 0
            "dark" -> 1
            "black" -> 2
            else -> 0  // system
        }
        
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                settings.theme = when (position) {
                    0 -> "light"
                    1 -> "dark"
                    2 -> "black"
                    else -> "system"
                }
                // Apply theme immediately
                (activity as? MainActivity)?.applyTheme(settings.theme)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Font size slider
        val fontSizeSlider = view.findViewById<Slider>(R.id.font_size_slider)
        fontSizeSlider.value = settings.fontSize.toFloat()
        
        fontSizeSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                settings.fontSize = value.toInt()
                // Update preview
                previewText.textSize = value
            }
        }
        
        // Reset button with debounce
        view.findViewById<Button>(R.id.reset_button).clickWithDebounce {
            settings.apply {
                enableBiometric = false
                theme = "system"
                fontSize = 14
            }
            refreshUI()
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check if authentication needed
        if (settings.enableBiometric && biometricLock.needsAuthentication()) {
            biometricLock.authenticate(
                activity = requireActivity(),
                title = "Re-authenticate",
                subtitle = "Session expired",
                onSuccess = {
                    biometricLock.resetAuthTimer()
                },
                onError = { code, _ ->
                    if (code != BiometricPrompt.ERROR_CANCELED) {
                        // Force logout or take other action
                    }
                }
            )
        }
    }
}
```

---

## 5️⃣ Common Patterns

### Singleton Pattern with Lazy Init

```kotlin
object AppConfig {
    private var instance: PreferenceHelper? = null
    
    fun init(context: Context) {
        instance = PreferenceHelper(context.applicationContext)
    }
    
    private val prefs: PreferenceHelper
        get() = instance ?: throw IllegalStateException("Not initialized")
    
    var apiEndpoint: String by prefs.string("api_endpoint", "https://api.example.com")
    var debugMode: Boolean by prefs.boolean("debug", false)
}
```

### Sealed Class for States

```kotlin
sealed class AuthState {
    object NotAuthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(val userId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Use in UI
fun renderState(state: AuthState) {
    when (state) {
        is AuthState.NotAuthenticated -> showLogin()
        is AuthState.Authenticating -> showProgress()
        is AuthState.Authenticated -> showMainScreen(state.userId)
        is AuthState.Error -> showError(state.message)
    }
}
```

### Coroutine Integration

```kotlin
class UserDataRepository(private val prefs: PreferenceHelper) {
    
    suspend fun loadUser(userId: Long): User {
        return withContext(Dispatchers.IO) {
            // Load from network or cache
            val cachedJson = prefs.getString("user_$userId", null)
            if (cachedJson != null) {
                parseUser(cachedJson)
            } else {
                val user = fetchFromNetwork(userId)
                prefs.save("user_$userId", toJson(user))
                user
            }
        }
    }
}
```

---

## 6️⃣ Troubleshooting

### Issue: Biometric not showing

**Solution**: Check permissions and device capabilities
```kotlin
when (biometricLock.getBiometricState()) {
    BiometricLock.BiometricState.HARDWARE_UNAVAILABLE -> {
        // Device has no biometric sensor
        // Fall back to PIN/password
    }
    BiometricLock.BiometricState.NO_ENROLLED -> {
        // Sensor exists but no fingerprints enrolled
        // Direct user to settings to enroll
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        startActivity(intent)
    }
    else -> {
        // Should work
    }
}
```

### Issue: Preferences not persisting

**Solution**: Ensure you're using `apply()` not just `edit()`
```kotlin
// ❌ Wrong - not saving
prefs.edit().putString("key", "value")

// ✅ Correct
prefs.edit().putString("key", "value").apply()

// Or use delegation
var value: String by prefs.string("key", "default")
value = "new value"  // Auto-saves
```

### Issue: View extensions not visible

**Solution**: Import the extension package
```kotlin
import tgx.extensions.*  // Add this import
```

---

## 7️⃣ Next Steps

After mastering these basics:

1. **Explore existing code** - See how Java classes use these utilities
2. **Create your own extensions** - Add project-specific helpers
3. **Migrate utilities** - Convert Java utils to Kotlin gradually
4. **Add features** - Implement new functionality in Kotlin
5. **Share knowledge** - Help team members adopt Kotlin

---

## 📚 Additional Resources

- [FORK_GUIDE.md](/FORK_GUIDE.md) - Complete fork development guide
- [KOTLIN_PROGRESS.md](/KOTLIN_PROGRESS.md) - Implementation roadmap
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Android KTX](https://developer.android.com/kotlin/ktx)

---

**Happy Coding! 🎉**
