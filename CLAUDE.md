# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build / Test Commands

Gradle wrapper is checked in; use `./gradlew` from the repo root.

- Build debug APK: `./gradlew assembleDebug`
- Install debug build on a connected device: `./gradlew installDebug`
- Build the signed release bundle: `./gradlew bundleRelease`

## Build Variants

Three build types defined in `app/build.gradle`, all installable side-by-side via distinct `applicationId` suffixes:

- `debug` → `net.engawapg.app.viewonlyviewer.d` — uses a debug-only `app_name` string override
- `staging` → `net.engawapg.app.viewonlyviewer.s` — minified like release but signed with the debug key
- `release` → `net.engawapg.app.viewonlyviewer` — minified + ProGuard, requires real signing config

Each variant has its own `res/values/strings.xml` to differentiate the launcher name.

## Architecture

Single-Activity Jetpack Compose app. The Activity (`MainActivity`) hosts a Compose `NavHost` with four destinations: `gallery`, `viewer/{index}`, `settings`, `setting_folder`. `NavController` is exposed app-wide via `LocalNavController` (a `staticCompositionLocalOf`), so screens deep in the tree can navigate without prop drilling.

**Dependency injection**: Koin. The single `appModule` is declared inline in `ViewOnlyViewerApp.kt` and provides two app-scoped singletons: `GalleryModel` and `SettingsRepository`. ViewModels resolve these via `KoinComponent.get()` rather than constructor injection — follow that pattern when adding new ViewModels.

**Data layer** (`data/` package):
- `GalleryModel` queries `MediaStore.Files` directly for images + videos, exposing results through `MutableSharedFlow` (replay=1). It supports excluding folders via an `ignoreFolderIds: Set<String>` argument to `load()`.
- `SettingsRepository` wraps a Preferences DataStore named `"settings"`. All settings are exposed as a single `AppSettings` flow; individual setters are suspend functions.

**UI layer** (`ui/` package): organized by feature (`gallery/`, `settings/`, `theme/`). Each feature pairs a `*Screen.kt` Composable with a `*ViewModel.kt`. The `theme/` package implements Material3 with optional dynamic color from wallpaper (controlled by `ColorThemeSetting`).

**Permissions**: Handled with `accompanist-permissions` in `AppContent`. The required permission set depends on SDK level — `READ_MEDIA_IMAGES` + `READ_MEDIA_VIDEO` on Tiramisu+, otherwise `READ_EXTERNAL_STORAGE`. If permission is denied past the rationale point, the app shows a "open Settings" screen rather than re-prompting.

**Fullscreen handling**: `MainActivity` sets `setDecorFitsSystemWindows(false)` and uses `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` so the viewer can smoothly transition into/out of fullscreen without layout jumps. Display cutout uses `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` on P+.
