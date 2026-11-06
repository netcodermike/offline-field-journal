# Validation record

Authoring host: Windows, November 6, 2026. Requirements: Kotlin 2.0.21 / Android Gradle Plugin 8.7.3; Android 8.0+ (API 26); build with JDK 17 and Android SDK 35.

The README verification command is `gradle :app:assembleDebug`. The companion GitHub Actions workflow describes reproducible build/test checks. Merely including that workflow does not mean it ran successfully. Host-specific verification results are recorded before publication.

Manual acceptance steps are in README.md. GUI behavior, hardware sensors, permissions, audio output, and OS isolation need verification on their target platform; compilation alone cannot establish these properties.

## Authoring result

Target toolchain/platform unavailable on the Windows authoring host. Source and build configuration were reviewed, and a target-platform GitHub Actions build/check workflow is provided. No successful native build or runtime result is claimed yet.

## Observed GitHub Actions result

Passed on November 6, 2026: [build/check run](https://github.com/netcodermike/offline-field-journal/actions/runs/42196330949).

Verified source commit: `99a4e0a8f481453ac4e111f878efb1ea7b593a6c`.

Android debug APK compilation using JDK 17, Gradle 8.9, and SDK 35.

This record may be committed after the linked source-validation run; the linked SHA identifies exactly what was checked. Native GUI/hardware acceptance remains manual as described in README.md.
