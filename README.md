# TidyDroid

**A Smart Download Organiser for Android.**

[![Build Debug APK](https://github.com/jnetaol/TidyDroid/actions/workflows/build-debug.yml/badge.svg)](https://github.com/jnetaol/TidyDroid/actions/workflows/build-debug.yml)
[![Build Release APK](https://github.com/jnetaol/TidyDroid/actions/workflows/build-release.yml/badge.svg)](https://github.com/jnetaol/TidyDroid/actions/workflows/build-release.yml)

## Features

- **Auto-Organize Downloads** - Automatically sorts files into Videos, APKs, Music, Documents, ZIPs, Images, Other
- **Custom Rules Engine** - Create regex-based sorting rules for any file type
- **Duplicate Finder** - Detect and remove duplicate files by size + hash
- **Large File Cleaner** - Find and delete large files wasting space
- **Scan History** - Track all organization scans with per-category stats

## Requirements

- Android 10+ (API 29+)
- ARM64 device
- Storage access permission for file management

## Installation

Download the latest APK from [Releases](https://github.com/jnetaol/TidyDroid/releases).

## Building

```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build (requires keystore)
```

## Tech Stack

- Kotlin 1.9.22, Jetpack Compose
- Room Database
- Material Design 3 (Dark Theme with purple/cyan neon)
- Gradle Kotlin DSL, AGP 8.2.2

## License

MIT License - see [LICENSE](LICENSE)

---

**Made By [jnetaol.com](https://jnetaol.com)**
