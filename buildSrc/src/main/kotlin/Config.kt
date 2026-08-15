/*
 * This file is a part of Telegram X
 * Copyright © 2014 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

// File with static configuration, that is meant to be adjusted only once

import tgx.gradle.fatal
import tgx.gradle.getLongOrThrow
import tgx.gradle.getOrThrow
import tgx.gradle.plugin.Keystore
import java.util.*

object Config {
  // OPTIMIZED FOR MODERN DEVICES - ARM64 ONLY, ANDROID 11+
  const val MIN_SDK_VERSION = 30  // Android 11 (R) - Modern devices only, no legacy support
  const val MIN_SDK_VERSION_HUAWEI = 30
  val JAVA_VERSION = org.gradle.api.JavaVersion.VERSION_21
  
  val ANDROIDX_MEDIA_EXTENSIONS = arrayOf(
    "decoder_ffmpeg",
    "decoder_flac",
    "decoder_opus",
    "decoder_vp9"
  )
  
  // ARM64-V8A ONLY - No legacy 32-bit support for smaller APK size and better performance
  val SUPPORTED_ABI = arrayOf("arm64-v8a")
  
  // Using shared STL for better memory usage on modern devices
  const val SHARED_STL = true
}

data class PullRequest (
  val id: Long,
  val commitShort: String,
  val commitLong: String,
  val commitDate: Long,
  val author: String
) {
  constructor(id: Long, properties: Properties) : this(
    id,
    properties.getOrThrow("pr.$id.commit_short"),
    properties.getOrThrow("pr.$id.commit_long"),
    properties.getLongOrThrow("pr.$id.date"),
    properties.getOrThrow("pr.$id.author")
  )
}

data class ApplicationConfig(
  val applicationName: String,
  val applicationId: String,
  val extension: String,
  val sourceCodeUrl: String,

  val applicationVersion: Int,
  val majorVersion: Int,

  val isExperimentalBuild: Boolean,
  val isHuaweiBuild: Boolean,
  val forceOptimize: Boolean,
  val doNotObfuscate: Boolean,

  val compileSdkVersion: Int,
  val targetSdkVersion: Int,
  val buildToolsVersion: String,

  val legacyNdkVersion: String,
  val primaryNdkVersion: String,

  val nativeLibraryVersion: String,
  val leveldbVersion: String,
  val emojiVersion: Int,

  val telegramApiId: Int,
  val telegramApiHash: String,
  val safetyNetToken: String?,
  val appDownloadUrl: String?,
  val googlePlayUrl: String?,
  val galaxyStoreUrl: String?,
  val huaweiAppGalleryUrl: String?,
  val amazonAppStoreUrl: String?,

  val pullRequests: List<PullRequest>,

  val outputFileNamePrefix: String,
  val creationDateMillis: Long,

  val keystore: Keystore?
)

data class AbiVariant(
  val flavor: String, 
  vararg val filters: String = arrayOf(), 
  val displayName: String = filters[0]
) {
  init {
    if (filters.isEmpty())
      fatal("Empty filters passed")
    for (filter in filters) {
      if (!Config.SUPPORTED_ABI.contains(filter))
        fatal("Unsupported abi filter: $filter")
    }
  }

  val is64Bit: Boolean
    get() {
      for (filter in filters) {
        if (filter != "arm64-v8a" && filter != "x86_64") {
          return false
        }
      }
      return true
    }

  val minSdk: Int
    get() = 30  // ARM64 on Android 11+ for modern features and optimizations
}

@Suppress("MemberVisibilityCanBePrivate")
object Abi {
  const val ARM64_V8A = 0  // Only ARM64 - optimized build

  // Single ABI configuration - ARM64 only
  val VARIANTS = mapOf(
    Pair(ARM64_V8A, AbiVariant("arm64", "arm64-v8a"))
  )
}

data class SdkVariant(
  val minSdk: Int = Config.MIN_SDK_VERSION,
  val maxSdk: Int? = null,
  val flavor: String,
  val displayName: String? = flavor
)

object Sdk {
  const val LATEST = 0  // Only latest SDK flavor needed

  // Single SDK variant - Android 11+ (API 30+)
  // No legacy support for smaller codebase and modern features only
  val VARIANTS = mapOf(
    Pair(LATEST, SdkVariant(
      flavor = "latest",
      minSdk = 30,
      displayName = null
    ))
  )
}

object App {
  // File extension to be detected as project's theme. Do not change unless you make changes to themes engine / add exclusive colors
  const val THEME_EXTENSION = "tgx-theme"
}

object Emoji {
  // Identifier of the built-in emoji set. Change it if built-in emoji pack is changed
  const val BUILTIN_ID = "apple"
}

object Telegram {
  // Cloud storage for emoji, icons, fonts, etc
  const val RESOURCES_CHANNEL = "Y2xvdWRfdGd4X2FuZHJvaWRfcmVzb3Vy"

  // Channel where to look up for new apks
  const val UPDATES_CHANNEL = "tgx_log"

  // Language pack on server. Do not change
  const val LANGUAGE_PACK = "android_x"
}
