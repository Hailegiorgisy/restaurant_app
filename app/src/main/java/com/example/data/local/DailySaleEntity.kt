package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores a full day's finalized or ongoing sales record in the Ethiopian calendar.
 */
@Entity(tableName = "daily_sales")
data class DailySaleEntity(
    @PrimaryKey val dateKey: String, // e.g. "2018-13-05" (Ethiopian Year-Month-Day)
    val ethiopianDateText: String,   // e.g. "ጳጉሜ 5 ቀን 2018 ዓ.ም"
    val dayOfWeek: String,           // e.g. "ሐሙስ"
    val timestamp: Long,
    val totalOrders: Int,
    val grossSales: Double,          // ጠቅላላ የሽያጭ ገቢ (ብር)
    val baseRevenue: Double,         // የምግብ/መጠጥ መነሻ ገቢ (ብር)
    val vatToRemit: Double,          // 15% ተጨማሪ እሴት ታክስ ለገቢዎች (ብር)
    val servicePool: Double,         // 10% የአገልግሎት ክፍያ ገንዳ (ብር)
    val settledCash: Double,         // የተረከቡት ጥሬ ገንዘብ (ብር)
    val pendingCash: Double,         // ያልተረከቡት / በእጅ ያለ ጥሬ ገንዘብ (ብር)
    val isClosed: Boolean = false,   // የቀን ሒሳብ ተቆልፏል / ተዘግቷል
    val notes: String = ""
)
