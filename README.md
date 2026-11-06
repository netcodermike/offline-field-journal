# Offline Field Journal

A native Android field journal that saves observations, copies photo attachments into app storage, adds cached location markers, and exports JSON without internet access.

**Language:** Kotlin 2.0.21 / Android Gradle Plugin 8.7.3  
**Platform:** Android 8.0+ (API 26); build with JDK 17 and Android SDK 35  
**Status:** First-release learning project. See implemented scope and limitations below; this is not a production-completeness claim.

## Features

- Title and multiline observation notes, timestamps, and a newest-first timeline.
- System document picker for photo attachments, copied into private app storage.
- Optional precise/approximate location permission and cached-fix marker with timestamp.
- Atomic JSON journal replacement and selectable entry text.
- Photo viewing and user-selected JSON export; no INTERNET permission.

## Requirements and quick start

Clone the repository, enter its folder, and install the language/platform tools listed above. Commands run from the repository root. No credentials are required except for Backup Vault's explicitly generated secrets.

```sh
git clone https://github.com/netcodermike/offline-field-journal.git
cd offline-field-journal
# Install Gradle 8.9, JDK 17, and Android SDK Platform 35.
# Set ANDROID_HOME or create local.properties with sdk.dir=YOUR_SDK_PATH.
gradle :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
# Or open this folder in Android Studio, select Gradle 8.9, sync, and Run.
```

## Verification

```sh
gradle :app:assembleDebug
```

On a device/emulator create a note, attach a photo, save, close, and reopen. Confirm text and image persist. Test permission denial and approximate-location permission. Export JSON through the document picker and inspect entries. Enable airplane mode and repeat note/photo operations. Rotate during an unsaved draft to understand the documented lifecycle limitation.

See [validation notes](docs/VALIDATION.md) for what was actually executed during authoring. A committed CI workflow is a future check until its run is observed successful.

## Architecture and data

`app/src/main/java/com/netcodermike/fieldjournal/MainActivity.kt` owns a platform-native Activity and vertical form. It uses `org.json`, Android location APIs, private internal files, and the Storage Access Framework. `journal.json` stores entry UUIDs, title, notes, epoch milliseconds, marker text, and attachment filenames. Photos are local copies named with UUIDs and capped at 20 MiB. The manifest requests only optional location permissions. Application backup is disabled. Gradle files pin the Kotlin/AGP build combination; Gradle 8.9 is required and a binary wrapper is not bundled.

## Limitations and next steps

This prototype uses cached location fixes, not an active GPS tracking session; markers may be stale and show their capture time. No maps, route tracking, editing/deletion, full-text search, journal import, or encrypted storage. JSON exports contain text/metadata and attachment filenames, not photo bytes; copy photos separately for a complete backup. Unsaved drafts can be lost on activity recreation. Large journals and image decoding can block the UI. A failed/cancelled attachment may leave an unused app-private file. Uninstalling removes private data.

## Troubleshooting

Use Gradle 8.9 and JDK 17 if sync fails. Install SDK 35 and accept SDK licenses through Android Studio. Denying location still permits notes/photos. No cached fix means open a location app outdoors, obtain a fix, then retry. If storage is full, the save error is shown and the form remains populated. A corrupted journal is preserved; restore its original contents before relaunching.

## Development

Read the source entry point described above, make a focused change, run the verification command, and exercise the relevant manual scenario. [Contributing](CONTRIBUTING.md) explains bug reports and pull requests; [security notes](SECURITY.md) describe the trust assumptions. Runtime/generated files are ignored. No telemetry or paid API dependencies are included.

## License

[MIT](LICENSE), copyright 2026 netcodermike.
