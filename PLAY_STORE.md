# MetricSphere Android — Google Play Store

Anleitung, um `ai.metricsphere.app` in den Play Store zu bringen.

## Aktueller App-Stand

| Feld | Wert |
|------|------|
| `applicationId` / namespace | `ai.metricsphere.app` |
| `minSdk` | 26 |
| `targetSdk` / `compileSdk` | 34 |
| `versionCode` | `1` (in `app/build.gradle.kts`) |
| `versionName` | `0.1.0` |
| Login | 6-stellige PIN (Desktop: „Geräte“) |
| UI | Native PIN-Login → WebView auf der Kunden-Instanz |

## 1. Google Play Console

1. Entwicklerkonto bei [Google Play Console](https://play.google.com/console) anlegen (einmalig ca. 25 €).
2. App erstellen: Name **MetricSphere**, Paketname **`ai.metricsphere.app`** (muss zur `applicationId` passen und ist danach praktisch unveränderlich).

## 2. Release-Signing (einmalig)

1. Upload-Keystore erzeugen (z. B. `metricsphere-upload.jks`).
2. **Nie** in Git committen (bereits in `.gitignore`: `*.keystore`, `*.jks`).
3. Passwort und Alias sicher verwahren (Passwort-Manager / Firmen-Vault).
4. In Gradle Release-Signing hinterlegen (lokal oder CI-Secrets).
5. Release als **Android App Bundle** bauen:

```bash
./gradlew bundleRelease
# Ausgabe typisch: app/build/outputs/bundle/release/app-release.aab
```

Debug-APKs reichen für den Store nicht.

## 3. Store-Listing

- Kurze und lange Beschreibung (DE; ggf. EN später)
- Screenshots (mind. Phone; ideal auch Tablet)
- Feature-Grafik / Icon gemäß Play-Vorgaben
- Kategorie z. B. Business oder Produktivität
- Kontakt-E-Mail
- **Datenschutzerklärung (URL)** — Pflicht (Login, Session, Netzwerk). z. B. Seite auf [metricsphere.ai](https://metricsphere.ai)

## 4. Data safety & Inhalt

- Formular **Datensicherheit**: Account-Login, Gerätebindung / Session, Internet; keine Werbung o. Ä. ehrlich angeben
- Zielgruppe und Content-Rating (IARC-Fragebogen)

## 5. Release-Tracks

1. Zuerst **Interner Test** oder **Geschlossener Test** (AAB hochladen, Tester einladen).
2. Nach erfolgreicher Prüfung **Produktion** freigeben.
3. Bei jedem neuen Store-Upload `versionCode` erhöhen (`1` → `2` → …); `versionName` semantisch anpassen (z. B. `0.1.1`).

Erste Prüfung oft **1–3 Tage** (beim allerersten Konto manchmal länger).

## Offene Punkte (technisch / inhaltlich)

- [ ] Upload-Keystore anlegen und Release-Signing in Gradle konfigurieren
- [ ] `bundleRelease` einmal lokal/CI verifizieren
- [ ] Privacy-Policy-URL festlegen und im Listing hinterlegen
- [ ] Store-Texte und Screenshots vorbereiten
- [ ] Data-safety-Formular ausfüllen
- [ ] `versionCode` / `versionName` vor dem ersten Upload prüfen

## Sideload (ohne Store)

Für Tests weiter Debug-APK nutzen — siehe [README.md](README.md).
