<<<<<<< HEAD
# 🎮 BattleGrounds GFX Tool

<div align="center">

![Version](https://img.shields.io/badge/Version-4.0-blue?style=for-the-badge&logo=android)
![SDK](https://img.shields.io/badge/Min%20SDK-24-green?style=for-the-badge&logo=android)
![Target SDK](https://img.shields.io/badge/Target%20SDK-35-brightgreen?style=for-the-badge&logo=android)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**A powerful graphics optimization tool for mobile battle royale games**

*Unlock higher FPS, enhance graphics, and dominate the battlefield!*

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Usage](#-usage) • [Supported Games](#-supported-games) • [How It Works](#-how-it-works) • [Contributing](#-contributing)

</div>

---

## 📖 Overview

**BattleGrounds GFX Tool** is an advanced Android application designed to optimize graphics settings for popular battle royale games like PUBG Mobile, BGMI, and their regional variants. The tool allows users to unlock higher frame rates (60, 90, and 120 FPS) and improve overall gaming performance without requiring root access.

Built with a sleek, gaming-inspired UI featuring neon aesthetics, RGB gradients, and HUD-style elements, this app provides an immersive experience for mobile gamers.

---

## ✨ Features

### 🚀 Performance Optimization
- **FPS Unlock**: Unlock 60, 90, and 120 FPS modes for smoother gameplay
- **Graphics Presets**: Apply optimized `.sav` configuration files
- **Real-time Monitoring**: View RAM, CPU, GPU, and storage usage in a gaming HUD-style dashboard

### 🎮 Game Support
- **Multi-Game Compatible**: Supports BGMI, PUBG Global, PUBG Korea, and PUBG Taiwan
- **Regional Variants**: Automatic detection and configuration for different game versions
- **Custom Save Files**: Upload and apply custom configuration files

### 🛡️ Advanced Features
- **Shizuku Integration**: Enhanced permissions for system-level modifications
- **SAF (Storage Access Framework)**: Non-root file modifications using Android's native APIs
- **Custom File/Folder Upload**: Import your own configuration presets

### 🎨 Premium UI/UX
- **Gaming Aesthetic**: Neon colors, RGB gradients, and cyberpunk-inspired design
- **Performance Dashboard**: Real-time system stats with animated progress indicators
- **Bottom Navigation**: Intuitive three-tab layout (Home, Dashboard, Settings)
- **Material Design 3**: Modern Android design principles with CardView components

---

## 📱 Screenshots

<div align="center">
<table>
  <tr>
    <td align="center"><b>Home Screen</b><br/>Performance Dashboard</td>
    <td align="center"><b>Game Selector</b><br/>Choose Your Battle Arena</td>
    <td align="center"><b>FPS Settings</b><br/>Unlock Frame Rates</td>
  </tr>
</table>
</div>

---

## 🎯 Supported Games

| Game | Package Name | Status |
|------|-------------|--------|
| **BGMI** (Battlegrounds Mobile India) | `com.pubg.imobile` | ✅ Full Support |
| **PUBG Global** | `com.tencent.ig` | ✅ Full Support |
| **PUBG Korea** | `com.pubg.krmobile` | ✅ Full Support |
| **PUBG Taiwan** | `com.rekoo.pubgm` | ✅ Full Support |

---

## 💾 Installation

### Prerequisites
- Android 7.0 (API 24) or higher
- Storage permissions
- Internet connection (for ads and updates)

### Method 1: Direct APK Install
1. Download the latest APK from the [Releases](../../releases) page
2. Enable "Install from Unknown Sources" in your device settings
3. Open the APK and follow the installation prompts

### Method 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/yourusername/battlegrounds-gfx-tool.git

# Open in Android Studio
# File -> Open -> Select the cloned directory

# Build the project
./gradlew assembleRelease

# The APK will be available at:
# app/release/app-release.apk
```

---

## 🔧 Usage

### Basic Usage

1. **Launch the App** - Open BattleGrounds GFX Tool
2. **Select Your Game** - Navigate to the Dashboard tab and choose your game version (BGMI, PUBG Global, Korea, or Taiwan)
3. **Choose FPS Setting** - Swipe through the available FPS options (60, 90, or 120 FPS)
4. **Apply Settings** - Watch a rewarded ad to apply the configuration
5. **Launch Game** - Start your game and enjoy improved performance!

### Using Shizuku (Advanced)

For enhanced functionality without root:

1. Install [Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) from Play Store
2. Start Shizuku via ADB/Wireless debugging
3. Grant permission when prompted in the app
4. Settings will now be applied directly without manual folder access

### Custom Configuration

1. Navigate to the Settings tab
2. Tap "Upload Custom File" or "Upload Custom Folder"
3. Select your custom `.sav` file or folder
4. Apply and launch your game

---

## ⚙️ How It Works

### Technical Overview

The app modifies game configuration files stored in:
```
/Android/data/{game_package_name}/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/
```

It replaces the `Active.sav` file with optimized presets that unlock:
- Higher FPS limits
- Improved graphics quality settings
- Optimized resource allocation

### Permission Methods

| Method | Requirements | Capabilities |
|--------|-------------|--------------|
| **SAF** | None | Access to game folders via Android's file picker |
| **Shizuku** | Shizuku app running | Direct file manipulation without manual selection |

---

## 📁 Project Structure

```
📦 BattleGrounds-GFX-Tool
├── 📂 app
│   ├── 📂 src/main
│   │   ├── 📂 assets              # FPS configuration presets
│   │   │   ├── Active.sav         # Default preset
│   │   │   ├── Active_60.sav      # 60 FPS preset
│   │   │   ├── Active_90.sav      # 90 FPS preset
│   │   │   └── Active_120.sav     # 120 FPS preset
│   │   ├── 📂 java/.../GfxTool
│   │   │   ├── MainActivity.java   # Navigation controller
│   │   │   ├── MainActivity2.java  # FPS application logic
│   │   │   ├── MainActivity3.java  # Custom upload handling
│   │   │   ├── ShizukuHelper.java  # Shizuku permission helper
│   │   │   └── 📂 ui               # Fragment UI components
│   │   │       ├── 📂 home         # Performance dashboard
│   │   │       ├── 📂 dashboard    # Game selector
│   │   │       └── 📂 notifications # Settings & links
│   │   └── 📂 res
│   │       ├── 📂 drawable         # Gaming-themed graphics
│   │       ├── 📂 layout           # XML layouts
│   │       └── 📂 values           # Colors, strings, themes
│   └── build.gradle.kts            # Dependencies & config
├── build.gradle.kts                # Project-level config
├── settings.gradle.kts
└── README.md
```

---

## 🛠️ Dependencies

| Library | Purpose |
|---------|---------|
| **AndroidX Navigation** | Fragment navigation |
| **Material Components** | Modern UI components |
| **CircleImageView** | Circular game icons |
| **Glide** | Efficient image loading |
| **SlideToAct** | Swipe action buttons |
| **Shizuku** | Non-root shell commands |
| **Unity Ads** | Rewarded advertisements |

---

## ⚠️ Disclaimer

> **IMPORTANT:** This tool is provided for educational and personal use only.

- This application is **NOT AFFILIATED** with PUBG Corporation, Krafton, or Tencent Games
- Use of third-party tools may violate the game's Terms of Service
- Users assume all responsibility for any consequences of using this tool
- The developers are not liable for any account bans or restrictions
- Always check your game's ToS before using optimization tools

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a new branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines
- Follow existing code style and naming conventions
- Test on multiple devices before submitting
- Update documentation for new features
- Keep the gaming aesthetic consistent in UI changes

---

## 📝 Changelog

### Version 4.0 (Current)
- 🎨 Complete UI redesign with gaming aesthetic
- ⚡ Added 120 FPS support
- 🛡️ Improved Shizuku integration
- 📱 Target SDK updated to 35
- 🎮 Added PUBG Taiwan support

### Version 3.x
- Added 90 FPS support
- SAF file access implementation
- Performance dashboard additions

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🔗 Links & Support

<div align="center">

[![Telegram](https://img.shields.io/badge/Telegram-Join%20Community-blue?style=for-the-badge&logo=telegram)](https://t.me/yourchannel)
[![YouTube](https://img.shields.io/badge/YouTube-Watch%20Tutorials-red?style=for-the-badge&logo=youtube)](https://youtube.com/@yourchannel)
[![GitHub Stars](https://img.shields.io/github/stars/yourusername/battlegrounds-gfx-tool?style=for-the-badge&logo=github)](../../stargazers)

</div>

---

<div align="center">

**Made with ❤️ for the gaming community**

*If you find this useful, please ⭐ the repository!*

</div>
=======
# BattleGrounds_GFX
BattleGrounds GFX Tool is a performance optimization app for games like PUBG Mobile, BGMI, and other battleground titles. It unlocks higher FPS options, optimizes graphics settings, reduces lag, and improves smoothness based on your device without changing core gameplay. Perfect for stable, competitive play.
>>>>>>> 9b3ffa7475d18554f3aef99a3840c05ca293ad2d
