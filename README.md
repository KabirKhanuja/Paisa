




## Stack

- Flutter (Material 3)
- Riverpod
- Firebase (Firestore + anonymous Auth)
- `local_auth` for biometrics
- `fl_chart` for the monthly chart

## Setup

1. `flutter pub get`
2. Configure Firebase:
   ```
   dart pub global activate flutterfire_cli
   flutterfire configure
   ```
   This generates `lib/firebase_options.dart`. Update [lib/main.dart](lib/main.dart) to import it and pass it to `Firebase.initializeApp`.
3. Enable **Anonymous** sign-in in Firebase Auth, and create a **Cloud Firestore** database.
4. `flutter run`

## Data model

```
users/
  {userId}/
    transactions/
      {txnId} — { amount, type: "earn"|"spend", timestamp }
    subscriptions/
      {subId} — { name, amount, cycle: "monthly"|"yearly" }
```

## Project layout

- [lib/main.dart](lib/main.dart) — bootstrap
- [lib/app.dart](lib/app.dart) — `MaterialApp` + theme wiring
- [lib/theme.dart](lib/theme.dart) — colors and Material 3 theme
- [lib/models/](lib/models/) — `Txn`, `Subscription`
- [lib/data/](lib/data/) — Firestore repositories + auth service
- [lib/providers.dart](lib/providers.dart) — Riverpod providers
- [lib/screens/](lib/screens/) — Lock, Home, Add, History, Subscriptions
- [lib/widgets/](lib/widgets/) — `BalanceCard`, `QuickStats`, `MonthlyChart`, `NumericKeypad`
