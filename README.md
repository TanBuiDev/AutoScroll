# Auto Scroll

Personal Android gesture automation app for configuring and running deterministic swipe gestures from a floating overlay.

Auto Scroll is designed for predictable user-configured workflows such as short-video feeds, reading pages, webtoons, horizontal feeds, and other apps where repeated swipe gestures are useful.

## Current Status

Current app version: **22.4.1**.

The core automation, per-app profile lifecycle, Room persistence, migration coverage, overlay controls, Accessibility disclosure/consent flow, Play release bundle validation, and CI release checks are implemented. The repository includes the policy and submission materials needed to prepare a Google Play release. Account-level Play Console declarations, reviewer assets, and signing secrets must still be completed by the developer before submission.

## Features

- Floating overlay control with Start/Stop, Next, Previous, Config, and gesture preview actions.
- Compact overlay with configurable opacity, size, orientation, and persisted position.
- Draggable expanded overlay with per-profile persisted position.
- Per-app profiles backed by Room with draft vs persisted state, dirty-state tracking, in-memory draft preservation when switching foreground apps, Edit/Delete/Reset actions, lifecycle status, presets, and Custom mode.
- Gesture configuration for next/previous intent, vertical/horizontal axis, distance, start X/Y, duration, and inverted physical direction.
- Timing modes: Once, Repeat, Until Stop, and Timer.
- Current screen shows completed swipe count, timer countdown, and why a run stopped.
- Accessibility readiness distinguishes an enabled permission from a connected service.
- English and Vietnamese resources are included together in Play bundles for manual language switching.
- Configurable start delay and stop-on-app-change behavior.
- Running automation restarts with the latest timing configuration when timing changes, while gesture configuration is read live.
- App settings persisted with DataStore.
- Manual language selection: system, English, Vietnamese.
- Theme selection: system, light, dark.
- Accessibility-service gesture dispatching without retrieving window content.
- Versioned prominent Accessibility disclosure and affirmative consent before opening Accessibility Settings.
- In-app link to the public Privacy Policy.
- Release AAB validation and optional environment-based upload signing.

## Screens

The main app is organized into four tabs:

- **Current**: permission state, active app/profile summary, gesture test, overlay controls, and dirty-aware Save.
- **Apps**: saved per-app scrolling settings with readable summaries, app icons, package details, a dedicated editor, delete and restore-default actions.
- **Scrolling**: select an installed app or use the detected app, then configure gestures and timing. Save/discard controls remain visible; leaving with unsaved changes asks for confirmation. Start scrolling with the floating play button.
- **Overlay**: floating control settings, theme, language, and advanced gated options.

## Profile Lifecycle

Profile state is intentionally explicit:

~~~text
new/default              -> Untested
Untested + edit          -> Untested
test gesture PASS        -> Tested
Tested + gesture/timing/
preset edit              -> NeedsReview
NeedsReview + edit       -> NeedsReview
successful retest        -> Tested
Save                     -> persists current status
~~~

Unsaved drafts are cached per package while the app process is alive, so switching between target apps does not silently discard edits.

## Timing Behavior While Running

Gesture changes are read dynamically for each dispatched gesture.

Timing changes use a different rule: **the current automation run is restarted with the new timing configuration**. This keeps mode, delay, start delay, repeat count, timer duration, and stop-on-app-change behavior consistent with the UI.

## Room Database

The database is currently at schema version 2.

- MIGRATION_1_2 adds overlay orientation and the unique package-name index.
- Instrumentation tests create a real version-1 database and run the migration to version 2.
- Room schema JSON files are committed under app/schemas.
- CI fails when generated Room schemas differ from committed schema files.

## Tech Stack

- Kotlin
- Android SDK 36
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore Preferences
- Kotlin Coroutines
- JUnit / AndroidX Test

## Requirements

- Android Studio or Android SDK command-line tools
- JDK 17
- Android device or emulator
- Android 8.0+ (minSdk 26)

## Build

From the repository root:

~~~powershell
.\gradlew.bat :app:assembleDebug
~~~

The debug APK is generated at:

~~~text
app/build/outputs/apk/debug/app-debug.apk
~~~

Release build verification:

~~~powershell
.\gradlew.bat :app:lintDebug :app:assembleRelease :app:bundleRelease
~~~

## Run Tests

Unit tests:

~~~powershell
.\gradlew.bat :app:testDebugUnitTest
~~~

Instrumentation tests, including Room migration validation:

~~~powershell
.\gradlew.bat :app:connectedDebugAndroidTest
~~~

## Install On A Connected Device

~~~powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
~~~

Then:

1. Enable Auto Scroll's Accessibility Service.
2. Grant "Display over other apps" permission.

For development, overlay permission can also be granted with:

~~~powershell
adb shell appops set com.personal.autoscroll SYSTEM_ALERT_WINDOW allow
~~~

## Permissions

Auto Scroll uses:

- SYSTEM_ALERT_WINDOW for the floating overlay.
- BIND_ACCESSIBILITY_SERVICE for deterministic swipe dispatch through Android Accessibility APIs.

The accessibility service listens for foreground window changes and performs configured gestures. It does **not** request window-content retrieval (canRetrieveWindowContent=false).

## Continuous Integration

GitHub Actions validates:

- unit tests
- debug build
- Android lint
- release build
- committed Room schemas
- instrumentation tests on an Android emulator
- Room migration 1 -> 2

## Google Play Release

The repository includes a versioned in-app Accessibility disclosure and consent flow. Test gestures and overlay automation are gated until consent is accepted. The Accessibility service keeps `canRetrieveWindowContent=false`.

Release and Play Console materials:

- [Release checklist](docs/RELEASE.md)
- [Privacy Policy](docs/PRIVACY_POLICY.md)
- [Data Safety worksheet](docs/DATA_SAFETY.md)
- [AccessibilityService declaration](docs/ACCESSIBILITY_DECLARATION.md)
- [Store listing copy](docs/STORE_LISTING.md)
- [Upload signing guide](docs/SIGNING.md)
- [Play Console submission checklist](docs/PLAY_CONSOLE_SUBMISSION.md)

Normal CI validates `:app:bundleRelease`. After upload-signing secrets are configured, the manual **Build Signed Play Bundle** GitHub Actions workflow builds and verifies a signed AAB without storing signing material in the repository.

## Repository Notes

- Main app module: app
- Package name: com.personal.autoscroll
- Current version: 22.4.1
- Database version: 2

## Disclaimer

This project is for personal Android automation and accessibility-driven gesture control. It is not intended to bypass payments, licensing, premium access, ads, security controls, or another application's access restrictions.
