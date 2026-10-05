# Release Checklist

This document is the release gate for Auto Scroll.

## Automated gates

A release candidate should not be published unless GitHub Actions is green for the target commit.

The Android CI workflow runs:

- unit tests
- Android lint
- debug APK assembly
- release APK assembly
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

Version 22.4.1 uses database schema version 2 and MIGRATION_1_2.

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

Before any Google Play release:

- review the current Google Play Accessibility API policy
- ensure the store listing accurately describes the automation use case
- complete any required Accessibility API declaration
- provide required in-app prominent disclosure and consent for non-accessibility-tool usage
- verify the app remains deterministic and user-configured rather than autonomously planning actions

Policy requirements can change independently of this repository, so re-check the current Play Console guidance for every store release.

## APK verification

For a candidate commit:

~~~powershell
.\gradlew.bat clean :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
~~~

For an emulator or connected test device:

~~~powershell
.\gradlew.bat :app:connectedDebugAndroidTest
~~~

Install the debug APK for smoke testing:

~~~powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
~~~

## Release metadata

Before tagging:

- confirm versionCode and versionName
- update README when user-visible capability or release requirements change
- add release notes describing behavior changes and migration impact
- confirm no generated Room schema changes are left uncommitted
- confirm CI is green on the exact commit being tagged

## Rollback considerations

If a release containing a Room migration must be rolled back, do not assume an older APK can open a database that has already migrated forward. Treat database downgrade behavior as a separate compatibility problem and test it explicitly before distributing a rollback build.
