# Android Upload Signing

Never commit an upload key, keystore password, key alias password, Play service-account credential, or decoded secret to this repository.

The repository supports release signing through environment variables.

## Required environment variables

~~~text
AUTOSCROLL_UPLOAD_STORE_FILE
AUTOSCROLL_UPLOAD_STORE_PASSWORD
AUTOSCROLL_UPLOAD_KEY_ALIAS
AUTOSCROLL_UPLOAD_KEY_PASSWORD
~~~

When all four values are present, the release build type uses that upload key. When they are absent, normal CI can still build an unsigned release artifact for compile validation.

## Create an upload key

Example:

~~~bash
keytool -genkeypair \
  -v \
  -keystore autoscroll-upload.jks \
  -alias autoscroll-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
~~~

Store the keystore and passwords outside the repository and back them up securely.

For Google Play, use Play App Signing and treat this key as the upload key rather than the Play app-signing key.

## Local signed AAB

Example on a local shell:

~~~bash
export AUTOSCROLL_UPLOAD_STORE_FILE="/secure/path/autoscroll-upload.jks"
export AUTOSCROLL_UPLOAD_STORE_PASSWORD="..."
export AUTOSCROLL_UPLOAD_KEY_ALIAS="autoscroll-upload"
export AUTOSCROLL_UPLOAD_KEY_PASSWORD="..."

./gradlew clean :app:testDebugUnitTest :app:lintDebug :app:bundleRelease
jarsigner -verify -strict app/build/outputs/bundle/release/app-release.aab
~~~

The bundle is generated at:

~~~text
app/build/outputs/bundle/release/app-release.aab
~~~

## GitHub Actions signed bundle

The manual **Build Signed Play Bundle** workflow expects these repository or environment secrets:

~~~text
PLAY_UPLOAD_KEYSTORE_BASE64
PLAY_UPLOAD_STORE_PASSWORD
PLAY_UPLOAD_KEY_ALIAS
PLAY_UPLOAD_KEY_PASSWORD
~~~

Encode the keystore for the first secret:

~~~bash
base64 -w 0 autoscroll-upload.jks
~~~

On macOS:

~~~bash
base64 < autoscroll-upload.jks | tr -d '\n'
~~~

The workflow decodes the key only into the temporary GitHub runner directory, builds the signed AAB, verifies the signature, and uploads the AAB as a short-lived workflow artifact.

## Key safety

- Do not reuse a personal keystore from another project.
- Do not put passwords in Gradle files or shell history.
- Do not commit `.jks`, `.keystore`, or `keystore.properties`.
- Keep an offline backup of the upload key.
- If the upload key is compromised, follow Google Play's upload-key reset process rather than changing the app-signing identity.
