# Runtime Reliability Implementation Plan

**Goal:** Correct Accessibility readiness, retain both selectable languages in Play bundles, and explain automation progress and stop reasons.

**Architecture:** Keep readiness in PermissionState, model run outcomes in AutomationState, and guard controller updates by session identity. Expose runtime state through the existing coordinator to the Current screen. Disable language resource splitting for the two supported languages.

**Tech Stack:** Kotlin, Compose, coroutines, JUnit, Android Gradle Plugin.

- [x] Add regression tests for permission readiness, failed dispatch, timed completion, cancellation, and rapid restart.
- [x] Connect service state only after Android binds the Accessibility service; distinguish enabled from connected in the UI.
- [x] Preserve English and Vietnamese resources in release bundles.
- [x] Add stop reasons, gesture count and timer countdown; prevent cancelled sessions from overwriting newer state.
- [x] Run unit tests, lint, debug APK and release bundle checks.
- [x] Install on the connected USB device and check launch and service state.
- [x] Verify live swipes after the user accepts the disclosure and grants Accessibility/overlay permissions.

**Verification:** 35 unit tests and 5 instrumentation tests passed on the connected Android 16 phone. Lint reports zero errors. The generated AAB's BundleConfig.pb confirms LANGUAGE splitting is disabled. The installed debug app launches successfully and initially shows Accessibility as required; no app crash was observed during launch.

**Local environment:** Existing build outputs have Windows ACLs from a different account SID. Verification uses a Gradle init script to redirect generated outputs to the shared tools area. No source-level build-directory override is committed. The installable candidate is copied to releases/AutoScroll-22.4.1-runtime-reliability-debug.apk.

**Live device verification:** After the user granted Accessibility and overlay access, screenshots confirmed Connected and Granted. A real Settings session completed one gesture and stopped on switching to the launcher with AppChanged and count 1. A saved Once profile completed one gesture with Completed and count 1. UIAutomator hierarchy dumps temporarily suppress other Accessibility services, so live checks used screenshots instead. The Settings test profile remains saved in Once mode; no continuous session remains active.
