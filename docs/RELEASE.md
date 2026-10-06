# Release Checklist

This document is the release gate for Auto Scroll.

## Automated gates

A release candidate should not be published unless GitHub Actions is green for the target commit.

The Android CI workflow runs:

- unit tests
- Android lint
- debug APK assembly
- release APK assembly
- release AAB assembly
- Room schema consistency check
- instrumentation tests
- Room migration 1 -> 2 validation

## Database safety

Before shipping a build that changes the Room schema:

1. Increment the database version.
2. Add explicit migrations for every supported upgrade path.
3. Export and commit the new schema JSON under app/schemas.
4. Add or update MigrationTestHelper coverage.
5. Verify existing user data survives the migration.
6. Never replace a required migration with destructive fallback for a production release.

Version 22.4.2 uses database schema version 2 and MIGRATION_1_2.

## Profile lifecycle checks

Before release, verify on a physical device:

- a new profile starts as Untested
- editing an Untested profile keeps it Untested
- a successful Test action marks the draft Tested
- changing gesture, timing, or preset after a successful test marks it NeedsReview
- Save persists the current status instead of forcing Tested
- switching between target apps preserves unsaved drafts during the current app process
- Edit, Delete, and Reset in Profiles update the active profile when applicable

## Automation runtime checks

Verify each timing mode:

- Once performs exactly one gesture
- Repeat respects repeat count
- Until Stop continues until stopped
- Timer stops at its configured duration
- start delay is applied before the first gesture
- stop-on-app-change stops the active run
- changing TimingConfig while running restarts the run with the new timing
- changing GestureConfig while running affects subsequent gestures without restarting
- rapid timing changes keep the newest run state; cancelled runs cannot overwrite it
- the Current screen retains the completed swipe count and the correct stop reason
- timer countdown starts after the configured start delay

Verify gesture fields:

- axis
- next/previous intent
- distance
- start X/Y
- swipe duration
- invert physical direction

## Overlay checks

Verify:

- compact overlay position persists through DataStore
- expanded overlay opens at its profile position
- dragging the expanded overlay updates expandedPositionX/Y
- profile Save persists expanded position
- overlay orientation, opacity, size, Next/Previous visibility, and auto-collapse behave as configured

## Accessibility hardening

The accessibility service must keep android:canRetrieveWindowContent set to false unless a future feature has a concrete, reviewed requirement to inspect accessibility node content.

Before any Google Play release verify:

- first-time Accessibility access shows the in-app prominent disclosure
- enabling Accessibility shows a waiting state until the service actually connects
- disconnecting the service stops automation with a service-disconnected reason
- the user must tap "I understand and agree" before Accessibility Settings opens
- Test and Overlay automation remain gated until consent is accepted
- Privacy Policy opens from both the disclosure and Advanced settings
- the store listing accurately describes the automation use case
- the AccessibilityService declaration matches docs/ACCESSIBILITY_DECLARATION.md
- the app remains deterministic and user-configured rather than autonomously planning actions

Policy requirements can change independently of this repository, so re-check the current Play Console guidance for every store release.

## APK verification

Verify both English and Vietnamese can be selected after installing from an AAB,
including when the device system language differs from the selected app language.
Language resource splitting is disabled so both translations remain available.

For a candidate commit:

~~~powershell
.\gradlew.bat clean :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease
~~~

For an emulator or connected test device:

~~~powershell
.\gradlew.bat :app:connectedDebugAndroidTest
~~~

Install the debug APK for smoke testing:

~~~powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
~~~

## Play submission materials

Before a Play release, review:

- docs/PRIVACY_POLICY.md
- docs/DATA_SAFETY.md
- docs/ACCESSIBILITY_DECLARATION.md
- docs/STORE_LISTING.md
- docs/SIGNING.md
- docs/PLAY_CONSOLE_SUBMISSION.md

Before Play submission, enable GitHub Pages with **GitHub Actions** as the source and run **Deploy Policy Pages** so the public Privacy Policy URL is live.

Use the manual **Build Signed Play Bundle** workflow only after GitHub upload-signing secrets are configured.

## Release metadata

Before tagging:

- confirm versionCode and versionName
- update README when user-visible capability or release requirements change
- add release notes describing behavior changes and migration impact
- confirm no generated Room schema changes are left uncommitted
- confirm CI is green on the exact commit being tagged

## Rollback considerations

If a release containing a Room migration must be rolled back, do not assume an older APK can open a database that has already migrated forward. Treat database downgrade behavior as a separate compatibility problem and test it explicitly before distributing a rollback build.

## GitHub preview v22.4.2

This GitHub prerelease uses the local test signing certificate, not a production/Play signing identity. Its release-build APK is installable and can update this machine's existing test-signed installation. Android may require uninstalling a build signed by a different key; export/back up any settings first.

Release contents: installable APK, SHA-256 checksums and the Figma 512×512 store icon. No keystore or signing passwords are uploaded. CI must succeed on the final main commit before publication. User verification of the updated UI and ColorOS background behavior remains pending.
