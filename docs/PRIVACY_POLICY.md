# Auto Scroll Privacy Policy

**Effective date: October 5, 2026**

Published policy URL: https://tanbuidev.github.io/AutoScroll/privacy/

Auto Scroll is a personal Android automation application that performs swipe gestures configured by the user. This policy explains what information the app processes, where that information is stored, and how Android AccessibilityService is used.

## Summary

Auto Scroll does not require an account and does not include advertising, analytics, tracking SDKs, or a developer-operated data upload service.

The current application does not request the Android INTERNET permission. Profile and settings data are stored locally on the device.

## Information processed on the device

Auto Scroll processes the following information locally:

- the package name and app label of the foreground application, so the matching per-app profile can be selected;
- gesture settings such as direction, axis, distance, start position, and duration;
- timing settings such as delay, start delay, repeat count, timer duration, and stop-on-app-change;
- overlay preferences and positions;
- profile state and preset selection;
- whether the current version of the Accessibility disclosure has been accepted.

This information is stored on the user's device using Room, Android DataStore, and a local app preference for the versioned Accessibility disclosure consent.

## AccessibilityService

Auto Scroll uses Android AccessibilityService for two narrowly defined purposes:

1. receiving foreground window-state change events so it can identify which app profile should be active; and
2. dispatching deterministic swipe gestures requested and configured by the user.

The AccessibilityService configuration sets `canRetrieveWindowContent=false`. Auto Scroll does not retrieve the accessibility node tree and does not read screen text, passwords, messages, form contents, or other on-screen content through AccessibilityService.

Auto Scroll does not autonomously decide what to click, purchase, send, post, or approve. Gesture behavior is determined by settings selected by the user.

## Overlay permission

Auto Scroll may request Android's "Display over other apps" permission so the floating control overlay can be shown above the target app.

## Data transmission and sharing

The current version of Auto Scroll does not transmit profile, settings, foreground-app, or AccessibilityService data to a developer server.

Auto Scroll does not sell or share user data with advertisers or data brokers.

Opening the public Privacy Policy link launches the user's browser. The browser and destination website may process data under their own privacy policies.

## Data retention and deletion

Profiles and preferences remain on the device until one of the following occurs:

- the user deletes or resets a profile;
- the user clears Auto Scroll's app data; or
- the user uninstalls Auto Scroll.

Because Auto Scroll does not maintain a remote account or developer database, there is no separate server-side account deletion process.

## Children

Auto Scroll is not designed to collect personal information from children. The current app does not transmit personal data to a developer server.

## Security

Auto Scroll minimizes Accessibility privileges by disabling window-content retrieval and keeps app configuration data local to the device. Android platform protections and the security of the user's device also affect local data security.

## Changes to this policy

If a future release adds network services, analytics, advertising, cloud synchronization, or materially changes AccessibilityService behavior, this policy and the Google Play Data Safety declaration must be updated before that release.

## Contact

For questions about this policy, use the public Auto Scroll repository issue tracker:

https://github.com/TanBuiDev/AutoScroll/issues
