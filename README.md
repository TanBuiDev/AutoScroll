# Auto Scroll

Personal Android gesture automation app for configuring and running repeatable swipe gestures from a floating overlay.

Auto Scroll is built for personal workflow automation such as short-video feeds, reading pages, horizontal feeds, and other apps where a user wants predictable manual or timed swipe gestures without relying on the target app's own automation features.

## Current Status

This repository is an MVP under active development. The app can be built, installed, and tested locally, but it is not yet prepared as a production Play Store release.

## Features

- Floating overlay control with Start/Stop, Next, Previous, and Config actions.
- Compact overlay with glass-style UI, configurable opacity, size, and vertical/horizontal layout.
- Quick settings modal for changing automation mode, direction, timing, swipe duration, overlay orientation, and opacity.
- Fullscreen gesture preview overlay that shows the actual swipe path on top of the current screen before running automation.
- Gesture configuration for:
  - content navigation target: next item or previous item
  - physical axis: vertical or horizontal
  - swipe distance
  - swipe duration
- Timing modes:
  - repeat with configurable repeat count
  - run until stopped
  - timer mode with configurable duration
- Per-app profile foundation using Room.
- App settings persisted with DataStore.
- Manual language selection: system, English, Vietnamese.
- Theme selection: system, light, dark.
- Accessibility-service based gesture dispatching.

## Screens

The main app is organized into four tabs:

- **Current**: permission status, current app summary, profile summary, overlay show/hide, save.
- **Profiles**: saved profiles by app.
- **Automation**: gesture and timing configuration.
- **Overlay**: floating control settings, theme, language, and advanced gated options.

## How Gesture Preview Works

The fullscreen preview uses the same gesture path calculation as the real accessibility gesture dispatch:

- screen width and height
- configured start point
- physical swipe direction
- configured distance percentage
- edge clamping when the path would exceed the screen
- configured swipe duration

This means the preview is intended to represent the gesture path that will actually be sent to Android.

## Tech Stack

- Kotlin
- Android SDK
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore Preferences
- Kotlin Coroutines
- JUnit

## Requirements

- Android Studio or Android SDK command line tools
- JDK 17
- Android device or emulator
- Android 8.0+ device target (`minSdk 26`)

## Build

From the repository root:

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Run Tests

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

## Install On A Connected Device

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Then grant the required permissions:

1. Enable the app's Accessibility Service in Android settings.
2. Grant "Display over other apps" permission.

For development, overlay permission can also be granted with:

```powershell
adb shell appops set com.personal.autoscroll SYSTEM_ALERT_WINDOW allow
```

## Permissions

Auto Scroll currently uses:

- `SYSTEM_ALERT_WINDOW`: required for the floating overlay.
- `BIND_ACCESSIBILITY_SERVICE`: required for dispatching swipe gestures through Android Accessibility APIs.

The app does not require target apps to expose APIs or automation endpoints. Gestures are generated at the Android system accessibility layer.

## Repository Notes

- Main app module: `app`
- UI prototype reference: `references/ui-prototype`
- Package name: `com.personal.autoscroll`
- Current version: `0.1.0`

## Development Commands

```powershell
# Build debug APK
.\gradlew.bat :app:assembleDebug

# Run unit tests
.\gradlew.bat :app:testDebugUnitTest

# Build and test
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

## Disclaimer

This project is for personal Android automation and accessibility-driven gesture control. It is not intended to bypass payments, licensing, premium access, ads, or security controls in other applications.
