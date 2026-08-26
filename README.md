# MetricSphere Android (Managed Login)

Native login shell (Slug + User + Password) → WebView on `https://{slug}.metricsphere.ai`.

## Requirements

- JDK 17+
- Android SDK (API 34) — or set `ANDROID_HOME`
- Optional: device with USB debugging

Local toolchain used on this machine (no sudo):

```bash
export JAVA_HOME="$HOME/.local/jdk"
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

## Build debug APK

```bash
cd MetricSphereAndroid
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Install on phone

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Login

1. Slug e.g. `template` or `upscaled`
2. Worker login email + password
