# Applying `restaurant_app_fixes.zip`

## Included Files
- `README.md` — Project overview, features, tech stack, and build/test commands.
- `.gitignore` — Preserves existing rules and ignores `*.apk` and `*.aab` build artifacts.
- `app/src/main/java/com/example/data/local/AppDatabase.kt` — Removes `fallbackToDestructiveMigration()` so future schema bumps require explicit migrations.
- `app/src/main/java/com/example/util/CurrencyMath.kt` — Exact `BigDecimal` (`HALF_UP`) forward/reverse tax math helper ready to be called from `VatCalculatorViewModel`.
- `app/src/main/res/values/strings.xml` — Standard UTF-8 English string resources.

## Steps to Apply & Push to GitHub

1. Extract `restaurant_app_fixes.zip` into the root of your local `restaurant_app` repository (overwrite existing files when prompted).
2. Remove the committed 16.5 MB debug APK from Git tracking (keeps the file locally if you want, or delete it) and commit:
   ```bash
   git rm --cached restaurant-calculator-debug.apk
   git add README.md .gitignore app/src/main/java/com/example/data/local/AppDatabase.kt app/src/main/java/com/example/util/CurrencyMath.kt app/src/main/res/values/strings.xml
   git commit -m "chore: add README, ignore APKs, add BigDecimal CurrencyMath, and remove destructive DB migration"
   git push origin main
   ```
