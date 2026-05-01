# Usland - GitHub Build Setup

## Quick Start

1. Push this repo to GitHub
2. Go to **Actions** tab - builds run automatically on push
3. Download APK from **Artifacts** section

## GitHub Actions Workflow

The workflow automatically:
- Builds debug APK on every push/PR
- Builds unsigned release APK
- Creates signed release when secrets are configured
- Publishes releases when you push a version tag (e.g., `v1.0.0`)

## Setting Up Signed Releases (Optional)

To enable signed APK builds:

### 1. Generate a Keystore

```bash
keytool -genkey -v -keystore usland-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias usland
```

### 2. Encode Keystore to Base64

```bash
base64 -i usland-release.jks | tr -d '\n' > keystore-base64.txt
```

### 3. Add GitHub Secrets

Go to **Settings > Secrets and variables > Actions** and add:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | Contents of keystore-base64.txt |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | `usland` (or your alias) |
| `KEY_PASSWORD` | Your key password |

## Creating a Release

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers the release workflow which:
- Builds the APK
- Creates a GitHub Release
- Attaches the APK as a downloadable asset

## Local Build

```bash
chmod +x gradlew
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK (unsigned without keystore)
```

APK locations:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Project Structure

```
usland/
├── .github/workflows/build.yml   # GitHub Actions workflow
├── app/
│   ├── build.gradle.kts          # App build config with signing
│   ├── proguard-rules.pro        # ProGuard/R8 rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/elsewhere/usland/
│       │   ├── MainActivity.kt
│       │   ├── service/OverlayService.kt
│       │   ├── overlay/*.kt      # UI components
│       │   ├── notification/     # Notification listener
│       │   ├── receiver/         # Boot & screen receivers
│       │   ├── settings/         # Settings screen
│       │   ├── state/            # State management
│       │   └── utils/            # Utilities
│       └── res/                  # Resources
├── gradle/
│   ├── libs.versions.toml        # Version catalog
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## Requirements

- JDK 17
- Android SDK 35
- Gradle 8.7 (wrapper included)
