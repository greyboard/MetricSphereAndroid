# MetricSphere Android

Native PIN-Login-Shell → WebView auf der Kunden-Instanz (managed oder Enterprise).

Die App braucht **keinen Slug und kein Passwort**: PIN aus dem Desktop-Dashboard unter **Geräte** eingeben. Session hält bis zu einem Jahr bzw. bis „Trennen“ im Desktop.

## Requirements

- JDK 17+
- Android SDK (API 34) — oder `ANDROID_HOME` setzen
- Optional: Gerät mit USB-Debugging

Lokale Toolchain (Beispiel ohne sudo):

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

## Login (PIN)

1. Im Browser auf der Instanz (Desktop) einloggen → **Geräte**
2. PIN anzeigen und in der App eingeben
3. Danach öffnet sich das Dashboard in der WebView

## Google Play Store

Schritte zu Signing, Listing, Data safety und Release-Tracks: **[PLAY_STORE.md](PLAY_STORE.md)**.
