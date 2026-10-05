# Google Play Data Safety Worksheet

This worksheet describes the repository state for Auto Scroll version 22.4.1. Re-check it against the exact release build before completing Play Console.

## Current build assessment

The current app:

- has no account system;
- does not request the Android INTERNET permission;
- includes no advertising SDK;
- includes no analytics or tracking SDK;
- does not upload profiles, settings, foreground app identity, or AccessibilityService information to a developer server.

Based on that implementation, the expected Play Console answers are:

| Play Console topic | Expected answer for current build |
| --- | --- |
| Does the app collect user data off-device? | No |
| Does the app share user data with third parties? | No |
| Is user data encrypted in transit? | Not applicable because the app does not transmit app data |
| Can users request deletion of an account? | Not applicable because the app has no account |
| Does the app provide a way to delete locally stored profiles? | Yes, through Profiles > Delete; clearing app data or uninstalling also removes local app data |

## Data processed only on device

The app locally processes:

- foreground package name and app label;
- profile configuration;
- gesture configuration;
- timing configuration;
- overlay preferences;
- Accessibility disclosure consent state.

These values are used to provide the app's local functionality and are not sent to a developer backend in the current build.

## Accessibility-specific note

The service receives window-state change events to identify the foreground package and uses `dispatchGesture` for user-configured swipes.

The service configuration uses:

~~~xml
android:canRetrieveWindowContent="false"
~~~

The current app does not read accessibility node content or screen text through AccessibilityService.

## Before every Play release

Re-evaluate this worksheet if any of the following are added:

- INTERNET permission;
- crash reporting that uploads diagnostics;
- analytics;
- advertising;
- cloud backup or sync controlled by the developer;
- authentication/accounts;
- remote configuration;
- telemetry;
- third-party SDKs that transmit identifiers or device data.

The submitted Data Safety form must describe the exact release artifact, not merely the intended architecture.
