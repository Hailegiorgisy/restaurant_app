# EthioVAT — Restaurant Calculator

Offline-first Android point-of-sale (POS) and Ethiopian tax calculator built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Room**.

## Features

* **Waiter POS (`WaiterPosScreen`):** Calculate itemized or quick-amount bills with configurable **10% service charge** and **15% Ethiopian VAT** (including VAT-on-service calculation).
* **Waiter Daily Collection (`WaitersDailyCollectionScreen`):** Track waiter shift totals, cash to collect, and settlement status.
* **Weekly & Daily Sales (`WeeklyDailySalesScreen`):** View 7-day Ethiopian calendar sales summaries.
* **Owner & Tax Summary (`OwnerTaxScreen`):** Review aggregate net revenue, service charge pool, and VAT liabilities.
* **Offline Persistence:** Stores receipts (`ReceiptEntity`) and daily sales (`DailySaleEntity`) locally in Room (`ethio_restaurant_vat_db`) with **zero Android permissions** required.

## Tech Stack

* **Language & UI:** Kotlin 2.2.10, Jetpack Compose (Material 3, BOM `2024.09.00`)
* **Architecture:** Single-module MVVM (`MainActivity` + `VatCalculatorViewModel` + `RestaurantRepository`)
* **Database:** Room 2.7.0 (KSP)
* **Testing:** JUnit 4, Robolectric 4.16.1, Roborazzi 1.59.0, Compose UI Test

## Setup & Build

1. Clone the repository and open it in **Android Studio**.
2. Copy `.env.example` to `.env` if needed for local secrets plugin configuration.
3. Sync Gradle and run the app on Android 7.0+ (`minSdk = 24`, `targetSdk = 36`):
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit and screenshot tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
