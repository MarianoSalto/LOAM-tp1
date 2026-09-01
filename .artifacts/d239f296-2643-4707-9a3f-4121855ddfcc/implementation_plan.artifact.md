# Fix "Failed to get service from broker" (Signature check failed)

The error `java.lang.SecurityException: Signature check failed for com.example.primertpdeappmoviles` occurs because the Google Play Services broker verifies the app's identity using its signing certificate (SHA-1 fingerprint). If the fingerprint of the current build (likely the debug build) is not registered in the Firebase or Google Cloud Console for your project, the connection to services like Firestore or Location is rejected.

## User Action Required

> [!IMPORTANT]
> This issue **cannot be fixed by code changes alone**. You must perform the following steps in the Firebase/Google Cloud Console:
> 1. **Get your SHA-1 fingerprint**: Open a terminal in Android Studio and run:
>    ```bash
>    ./gradlew signingReport
>    ```
>    Look for the `SHA1` value under the `debug` variant.
> 2. **Register it in Firebase**:
>    - Go to the [Firebase Console](https://console.firebase.google.com/).
>    - Select your project (`loam-mds`).
>    - Click on the gear icon (Project Settings).
>    - Scroll down to "Your apps" and select `com.example.primertpdeappmoviles`.
>    - Click **Add fingerprint** and paste your SHA-1.
> 3. **Update config**: Download the new `google-services.json` and replace the existing one in the `app/` folder.

## Proposed Changes

I will also perform some cleanup in the build configuration to remove redundant dependencies and fix non-standard declarations.

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/usuario/AndroidStudioProjects/PrimerTpdeAppMoviles/app/build.gradle.kts)
- Remove `libs.play.services.maps` (redundant as the app uses MapLibre).
- Standardize `compileSdk` and `targetSdk` to use stable values (e.g., 35).
- Fix the `compileSdk { version = release(37) }` block which is non-standard.

## Verification Plan

### Manual Verification
- Run `./gradlew signingReport` and verify the SHA-1 matches the console.
- Deploy the app to a device/emulator and check if the "Failed to get service from broker" error persists when interacting with Map or Firestore.
