package com.example.model

enum class Language {
    AMHARIC,
    ENGLISH
}

enum class CalculationType {
    FORWARD,
    REVERSE,
    ITEMIZED
}

data class TaxCalculation(
    val baseAmount: Double = 0.0,
    val serviceRate: Double = 0.10, // 10% standard in Ethiopian restaurants when enabled
    val serviceAmount: Double = 0.0,
    val vatRate: Double = 0.15, // 15% standard Ethiopian VAT
    val vatAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val vatOnService: Boolean = true, // Tax Authority rule: VAT levied on (Base + Service)
    val includeServiceCharge: Boolean = true // Service charge is optional
)

data class OrderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nameEn: String = "",
    val nameAm: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}

data class SavedReceipt(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val ethiopianDateKey: String = "",
    val ethiopianDateText: String = "",
    val ethiopianTimeText: String = "",
    val ethiopianDayOfWeek: String = "",
    val calculationType: CalculationType = CalculationType.ITEMIZED,
    val restaurantName: String = "",
    val waiterName: String = "",
    val tableNumber: String = "",
    val baseAmount: Double,
    val serviceRate: Double,
    val serviceAmount: Double,
    val vatRate: Double,
    val vatAmount: Double,
    val totalAmount: Double,
    val vatOnService: Boolean,
    val includeServiceCharge: Boolean = true,
    val note: String = "",
    val isSettled: Boolean = false // Whether the cashier has received the cash from this waiter
)

data class WaiterShiftReport(
    val waiterName: String,
    val totalOrders: Int,
    val totalCashToCollect: Double,
    val totalBaseRevenue: Double,
    val totalVat: Double,
    val totalServiceCharge: Double,
    val settledOrders: Int = 0,
    val pendingCash: Double = 0.0
)

data class DailySaleSummary(
    val dateKey: String,
    val ethiopianDateText: String,
    val dayOfWeek: String,
    val timestamp: Long,
    val totalOrders: Int,
    val grossSales: Double,
    val baseRevenue: Double,
    val vatToRemit: Double,
    val servicePool: Double,
    val settledCash: Double,
    val pendingCash: Double,
    val isClosed: Boolean = false,
    val notes: String = ""
)
