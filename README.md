basically i made this to save stipend money for a better cause (diet coke)

<img width="612" height="440" alt="image" src="https://github.com/user-attachments/assets/a5aa8a56-cbfe-4d51-8b44-b44c585867a8" />


yes it seems like just another financial tracking app (trust me it is)


## THE FEATURES?!!!!

1. AD FREE WOW
2. NO VENDOR LOCK IN WOW
3. IT'S SIMPLE AND NICE
4. Third point is commendable
5. IT'S MANAUAL SO MAKES YOU REALIZE THAT YOU'RE SPENDING WAY TOO MUCH (i'll implement scraping through msgs later lol)
6. OPEN SOURCE WOW FREE
7. I MADE IT (thanks mr.opus)

---

## Build & run

This is an Android app (Kotlin + Jetpack Compose). Package `kabir.paisa`.

1. Open the project root in Android Studio (Iguana or newer).
2. Drop your Firebase `google-services.json` into `app/` (it's gitignored).
3. Sync Gradle. Android Studio will fetch the Gradle wrapper jar on first sync — or run `gradle wrapper --gradle-version 8.7` from the project root if you have a system Gradle.
4. Run on a device or emulator with API 26+.

Notification listener access must be granted from system Settings (Settings → Notifications → Device & app notifications → Special access). The Settings screen in-app provides a shortcut.

### Project structure

```
app/src/main/java/kabir/paisa/
├── MainActivity.kt, PaisaApp.kt
├── nav/                 # NavHost & routes
├── ui/theme/            # colors, typography, shapes
├── common/              # currency, date, shared composables
├── data/                # repositories + models (Firestore-backed)
├── auth/                # login / signup
├── home/                # balance hero + recent transactions
├── amount/              # transaction log, add/subtract entry, category picker
├── budget/              # overview + setup
├── analytics/           # calendar, costliest day, category bars
├── settings/            # bank, permissions, preferences, logout
└── notifications/       # NotificationListenerService + bank parser + tagging UI
```

UI design lives in `design/*.html` — those are the visual spec.

