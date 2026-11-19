package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val ethiopianDateKey: String,   // e.g. "2018-13-05"
    val ethiopianDateText: String,  // e.g. "ጳጉሜ 5 ቀን 2018 ዓ.ም"
    val ethiopianTimeText: String,  // e.g. "ከሰዓት 8:30"
    val ethiopianDayOfWeek: String, // e.g. "ሐሙስ"
    val restaurantName: String,
    val waiterName: String,
    val tableNumber: String,
    val baseAmount: Double,
    val serviceRate: Double,
    val serviceAmount: Double,
    val vatRate: Double,
    val vatAmount: Double,
    val totalAmount: Double,
    val vatOnService: Boolean,
    val includeServiceCharge: Boolean,
    val note: String = "",
    val isSettled: Boolean = false
)
