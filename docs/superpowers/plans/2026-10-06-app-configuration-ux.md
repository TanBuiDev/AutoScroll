# App Configuration UX Implementation Plan

**Goal:** Make per-app scrolling configuration understandable and keep edits in a dedicated screen.
**Architecture:** Use a full-screen Compose dialog for saved app editing, separate tab scroll state, and an app chooser for the active configuration. Keep package identity unchanged and guard unsaved edits.
**Tech Stack:** Kotlin, Compose, existing ViewModels and profile controller.

- [x] Replace technical terminology, explain activation, empty states and gesture test results in both languages.
- [x] Add app identity headers with icons and readable scrolling summaries.
- [x] Open saved configuration in a dedicated editor with fixed save/back controls and discard confirmation.
- [x] Separate tab scroll state and guard navigation away from unsaved configuration.
- [x] Add app selection and Accessibility guidance when no app is detected.
- [x] Build the APK and update the connected phone. Automated tests are not requested for this change.

**Outcome:** Debug APK assembled successfully using the existing wrapper, shared SDK and local build-output redirect. Updated the connected Android 16 phone successfully and opened the app. Screenshots inspected for the Apps list, dedicated editor and no-selected-app Scrolling state. No automated tests were run for this change; save/discard and app chooser interaction flows are implemented but have not been exercised end to end. No Git commit or push performed.

**Blank screen finding:** The old tab relies on an external Accessibility window event to select a profile; after process restart there may be no selected app even when the service is bound. Tabs also shared scroll position. The new UI separates scrolling state and offers explicit installed-app selection plus permission/empty-state guidance.
