# Google Play AccessibilityService Declaration

Use this document as the source of truth when completing the AccessibilityService declaration and reviewer notes in Play Console.

## Is Auto Scroll an accessibility tool?

**No.**

Auto Scroll is a user-configured gesture automation utility. It should not be represented as an app whose primary purpose is assisting users with disabilities unless the product is materially redesigned for that purpose.

## Core use case

Auto Scroll lets the user configure deterministic swipe gestures for a selected app profile. The user chooses the gesture direction, axis, distance, starting position, timing mode, delays, repeat behavior, and whether automation should stop when the foreground app changes.

The AccessibilityService is necessary because Android's `dispatchGesture` capability is provided through AccessibilityService.

## Accessibility data accessed

The service listens to `TYPE_WINDOW_STATE_CHANGED` events and reads the event package name to detect the foreground application.

The service is configured with:

~~~xml
android:canRetrieveWindowContent="false"
android:canPerformGestures="true"
~~~

It does not retrieve the accessibility node tree or read screen text, passwords, messages, or form contents.

## Actions performed

The service dispatches swipe gestures using parameters selected by the user.

It does not autonomously:

- interpret screen content;
- choose products or purchases;
- send messages or posts;
- approve transactions;
- bypass security controls;
- make open-ended plans or decisions on the user's behalf.

## Prominent disclosure and consent

Before Auto Scroll opens Android Accessibility Settings for a user who has not accepted the current disclosure version, the app shows an in-app disclosure explaining:

- that AccessibilityService is used to perform configured swipe gestures;
- that foreground app identity is read from window-state change events;
- that window content and accessibility node data are not retrieved;
- what actions the service performs;
- that profile/settings data remain local in the current version;
- that the user can cancel instead of enabling the service.

The user must press **"I understand and agree"** before the app opens Accessibility Settings. Consent is stored locally with a disclosure version so a future material disclosure change can require consent again.

## Suggested reviewer video

Record one continuous video showing:

1. launch Auto Scroll;
2. tap Accessibility in the Current tab;
3. show the full prominent disclosure;
4. open the Privacy Policy link if practical;
5. tap "I understand and agree";
6. enable Auto Scroll in Android Accessibility Settings;
7. return to Auto Scroll;
8. grant overlay permission;
9. configure a profile;
10. start the overlay;
11. demonstrate deterministic swipe automation;
12. stop automation.

Do not edit the video in a way that hides the disclosure or the Android permission flow.

## Suggested Play reviewer note

> Auto Scroll uses Android AccessibilityService only to identify the foreground app from window-state change events and to dispatch deterministic swipe gestures configured by the user. Window-content retrieval is disabled (`canRetrieveWindowContent=false`). The app does not inspect accessibility node content and does not autonomously make decisions. A prominent in-app disclosure and affirmative consent are shown before opening Accessibility Settings.
