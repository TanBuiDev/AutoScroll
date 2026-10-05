# Google Play Console Submission Checklist

This repository can prepare and validate the release artifact, but account-level Google Play actions still require the developer's Play Console credentials and decisions.

## Repository-provided materials

Use these files when completing Play Console:

- Privacy Policy: https://tanbuidev.github.io/AutoScroll/privacy/
- Data Safety worksheet: `docs/DATA_SAFETY.md`
- AccessibilityService declaration: `docs/ACCESSIBILITY_DECLARATION.md`
- Store listing copy: `docs/STORE_LISTING.md`
- Upload signing guide: `docs/SIGNING.md`
- Release checks: `docs/RELEASE.md`

## 1. Developer account and package

In Play Console:

1. Complete any developer identity/account verification currently required for the account.
2. Create or select the Auto Scroll app.
3. Confirm the package name is exactly:
   - `com.personal.autoscroll`
4. Enable Play App Signing.
5. Do not change the application ID after the first Play release.

If Play Console shows account-specific testing or verification requirements, complete those before requesting Production access.

## 2. Privacy Policy

Set the Play Console Privacy Policy URL to:

~~~text
https://tanbuidev.github.io/AutoScroll/privacy/
~~~

Before submission, open that URL in a logged-out/incognito browser and confirm it is publicly readable, globally accessible, served as HTML, and not an editable document.

The same policy is linked from the app's Accessibility disclosure and Advanced settings.

## 3. Data Safety

Complete Data Safety using `docs/DATA_SAFETY.md` as the implementation worksheet.

For the current build, the expected state is:

- no developer-side collection of app/user data;
- no third-party sharing;
- no ads or analytics;
- local profile/settings processing only.

Re-evaluate the form if the release adds networking, telemetry, crash upload, analytics, ads, accounts, cloud sync, or a third-party SDK that transmits data.

## 4. AccessibilityService declaration

Use `docs/ACCESSIBILITY_DECLARATION.md`.

Key declaration facts:

- Auto Scroll is **not** presented as an accessibility tool.
- The service is used for user-configured deterministic swipe automation.
- It reads the foreground package from window-state change events.
- `canRetrieveWindowContent=false`.
- It dispatches gestures according to user-selected settings.
- It does not inspect accessibility node content.
- It does not make open-ended decisions on behalf of the user.
- The app shows a prominent disclosure and requires affirmative consent before opening Accessibility Settings.

Upload a reviewer video demonstrating the full disclosure, consent, Android settings enablement, profile configuration, overlay, and deterministic swipe behavior.

## 5. App content declarations

Complete all Play Console sections shown for the account and release, including as applicable:

- Ads: current build contains no ads.
- App access: describe any steps reviewers need to enable Accessibility and overlay permission.
- Content rating.
- Target audience and content.
- Data Safety.
- Privacy Policy.
- AccessibilityService declaration.
- Any permissions or policy declarations Play Console requests for this package.

Do not claim the app is an accessibility tool unless the product's primary purpose changes accordingly.

## 6. Store listing

Use `docs/STORE_LISTING.md` as the approved copy source.

Prepare separately in Play Console:

- app icon;
- phone screenshots;
- feature graphic;
- support/developer contact details required by the account;
- localized listing assets as desired.

Screenshots should accurately show the current UI and should not hide the Accessibility disclosure.

## 7. Upload signing secrets

Configure the GitHub Secrets documented in `docs/SIGNING.md`:

~~~text
PLAY_UPLOAD_KEYSTORE_BASE64
PLAY_UPLOAD_STORE_PASSWORD
PLAY_UPLOAD_KEY_ALIAS
PLAY_UPLOAD_KEY_PASSWORD
~~~

Never paste these values into source files, PR comments, issues, release notes, or logs.

## 8. Build the signed AAB

After the signing secrets exist on the default branch:

1. Open GitHub Actions.
2. Run **Build Signed Play Bundle** manually.
3. Confirm the workflow passes unit tests and lint.
4. Confirm the AAB signature verification step passes.
5. Download the generated short-lived artifact.
6. Upload `app-release.aab` to the intended Play track.

The normal Android CI also builds `:app:bundleRelease` without requiring signing secrets, so AAB generation stays continuously validated.

## 9. Reviewer instructions

Suggested reviewer path:

1. Install/open Auto Scroll.
2. Open **Current**.
3. Tap **Accessibility**.
4. Read the in-app disclosure.
5. Tap **I understand and agree**.
6. Enable Auto Scroll in Android Accessibility Settings.
7. Return to Auto Scroll.
8. Grant Display over other apps.
9. Open a target app once so a profile becomes active.
10. Configure Automation if desired.
11. Show the overlay.
12. Start and stop deterministic swipe automation.

Tell the reviewer that window-content retrieval is disabled and that the service only uses foreground app identity plus gesture dispatch.

## 10. Track progression

Start with the least risky track available to the account:

1. Internal testing
2. Closed testing when required/useful
3. Open testing if desired
4. Production after all account-specific eligibility and policy review requirements are satisfied

Testing-track eligibility can depend on Play account history and current Play Console policy. Follow the requirements shown in the account rather than assuming a fixed tester count or duration.

## 11. Final production gate

Do not promote to Production unless all of the following are true:

- CI is green for the exact release commit.
- Signed AAB is generated from that commit.
- versionCode/versionName are correct and unique for the Play release.
- Privacy Policy URL is public.
- Data Safety matches the exact artifact.
- Accessibility declaration and reviewer video match actual behavior.
- Store listing is accurate.
- No signing material is committed.
- Play Console has no unresolved policy or app-content tasks.
