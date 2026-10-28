# PreMost

A high-performance, single-stream HLS viewer built for Android using **Jetpack Compose (Material 3 Expressive)** and **AndroidX Media3 (ExoPlayer)**.

Designed to eliminate web bloat, broken desktop media engines, and invasive advertisements by bridging direct HLS manifests straight to a native, edge-to-edge hardware-accelerated playback surface.

---

## Highlights

* **Zero-Brand Coupling:** Does not bundle, ship with, or hardcode proprietary third-party endpoints. Configured dynamically on device.
* **Instant Cold Starts:** Prompts for stream parameters on first launch, caches securely via **Jetpack DataStore Preferences**, and bypasses all menus on subsequent boots straight into live video.
* **Hardware-Accelerated Low Latency:** Powered by Media3 ExoPlayer with chunkless HLS playlist preparation enabled.
* **Resilient Network Adapter:** Supports optional dynamic HTTP header injection (`Referer`, `Origin`, `User-Agent`) to comply with upstream CDN hotlink protection policies.
* **Ergonomic, Accessible UI:** Built with Material 3 Expressive components, full-bleed landscape immersion, wake-lock persistence (`FLAG_KEEP_SCREEN_ON`), and large tap targets (>64dp) for seamless use by non-technical audiences.

---

## Tech Stack & Architecture

* **Language:** Kotlin 2.x
* **UI Toolkit:** Jetpack Compose + Material 3 Expressive (`M3E`)
* **Media Engine:** AndroidX Media3 (`media3-exoplayer`, `media3-exoplayer-hls`, `media3-ui`)
* **Persistence:** Jetpack DataStore Preferences
* **Architecture:** Unidirectional Data Flow (MVI/MVVM pattern) with strict state encapsulation (`Unconfigured`, `Loading`, `Playing`, `Error`)


```

app/
├── data/
│   ├── StreamPreferencesRepository.kt  # DataStore persistence layer (URL & HTTP headers)
│   └── StreamMediaSourceFactory.kt     # ExoPlayer HlsMediaSource factory & header interceptor
├── presentation/
│   ├── WebcamUiState.kt                # Immutable UI state definitions
│   ├── WebcamViewModel.kt              # Playback controller, state machine & retry policy
│   ├── WebcamScreen.kt                 # Adaptive edge-to-edge surface & M3E overlays
│   └── components/
│       ├── PlayerSurface.kt            # AndroidView wrapping PlayerView with aspect ratio control
│       ├── SetupDialog.kt              # M3E modal configuration sheet with quick-paste
│       └── StatusOverlays.kt           # Loading spinners, error cards, and quick settings triggers
└── MainActivity.kt                     # Window flags, system bars control & lifecycle hooks

```

---

## Getting Started

### Prerequisites
* Android Studio Ladybug (or newer)
* Android SDK 35+
* Minimum Android SDK: 26 (Android 8.0 Oreo)

### Build & Install
Clone the repository and build the debug APK via Gradle:

```bash
git clone [https://github.com/](https://github.com/)<username>/premost.git
cd premost
./gradlew assembleDebug

```

Push to your target device via ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk

```

---

## Usage

1. **First Launch:** The app opens an initial setup sheet.
2. **Stream URL:** Paste your direct HLS manifest endpoint (e.g., `https://.../playlist.m3u8`).
3. **HTTP Headers (Optional):** If the upstream CDN mandates origin validation, supply the required `Referer` or `Origin` headers in the advanced section.
4. **Save & Play:** Tap **Shrani in predvajaj** (Save & Play). The configuration persists locally.
5. **Editing:** Tap the subtle settings icon in the top corner of the stream at any time to update parameters or point to a new manifest.

---

## Legal & Operational Disclaimer

This software is a generic, open-source media player utility provided under the MIT License.

* **No Content Hosting:** PreMost does not host, cache, re-encode, or distribute any media or proprietary video streams.
* **User Responsibility:** Users must provide their own stream endpoints and ensure their consumption complies with the terms of service of the respective network providers.
* **Trademarks:** All brand names, logos, and endpoints referenced or accessed by end users are the property of their respective owners.
