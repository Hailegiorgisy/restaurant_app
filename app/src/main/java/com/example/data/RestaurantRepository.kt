package com.example.data

import com.example.data.local.DailySaleEntity
import com.example.data.local.ReceiptEntity
import com.example.data.local.RestaurantDao
import com.example.util.EthiopianDateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class RestaurantRepository(private val dao: RestaurantDao) {

    val allReceipts: Flow<List<ReceiptEntity>> = dao.getAllReceipts()
    val weeklyDailySales: Flow<List<DailySaleEntity>> = dao.getWeeklyDailySales()
    val allDailySales: Flow<List<DailySaleEntity>> = dao.getAllDailySales()

    suspend fun saveReceipt(receipt: ReceiptEntity) {
        dao.insertReceipt(receipt)
    }

    suspend fun settleWaiterSales(waiterName: String) {
        dao.settleAllForWaiter(waiterName)
    }

    suspend fun toggleReceiptSettled(id: String, isSettled: Boolean) {
        dao.updateReceiptSettled(id, isSettled)
    }

    suspend fun deleteReceipt(id: String) {
        dao.deleteReceipt(id)
    }

    suspend fun clearAllReceipts() {
        dao.deleteAllReceipts()
    }

    suspend fun saveDailySale(dailySale: DailySaleEntity) {
        dao.insertDailySale(dailySale)
    }

    suspend fun deleteDailySale(dateKey: String) {
        dao.deleteDailySale(dateKey)
    }

    /**
     * Seeds initial records for the past 7 Ethiopian days if database is empty.
     */
    suspend fun seedInitialWeeklySalesIfEmpty() {
        val currentWeekly = dao.getWeeklyDailySales().firstOrNull()
        if (!currentWeekly.isNullOrEmpty()) return

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }

        val seedDailySales = mutableListOf<DailySaleEntity>()
        val seedReceipts = mutableListOf<ReceiptEntity>()

        // Seed for 7 consecutive days up to today
        val dayData = listOf(
            Triple(0, 18500.0, 14),  // Today
            Triple(-1, 24200.0, 19), // Yesterday
            Triple(-2, 19800.0, 16), // 2 days ago
            Triple(-3, 31400.0, 25), // 3 days ago (Weekend/Peak)
            Triple(-4, 28900.0, 22), // 4 days ago
            Triple(-5, 16200.0, 13), // 5 days ago
            Triple(-6, 21500.0, 17)  // 6 days ago
        )

        for ((dayOffset, gross, orders) in dayData) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dayMillis = dayCal.timeInMillis
            val ethDate = EthiopianDateUtil.toEthiopianDate(dayMillis)

            val baseRev = gross / (1.10 * 1.15)
            val servicePool = baseRev * 0.10
            val vatToRemit = (baseRev + servicePool) * 0.15
            val isClosed = dayOffset < 0 // Previous days are closed/finalized

            seedDailySales.add(
                DailySaleEntity(
                    dateKey = ethDate.dateKey,
                    ethiopianDateText = ethDate.formattedDate,
                    dayOfWeek = ethDate.dayOfWeekName,
                    timestamp = dayMillis,
                    totalOrders = orders,
                    grossSales = gross,
                    baseRevenue = baseRev,
                    vatToRemit = vatToRemit,
                    servicePool = servicePool,
                    settledCash = if (isClosed) gross else gross * 0.70,
                    pendingCash = if (isClosed) 0.0 else gross * 0.30,
                    isClosed = isClosed,
                    notes = if (isClosed) "የተጠናቀቀ የቀን ሒሳብ" else "የዛሬ ክፍት ፈረቃ"
                )
            )

            // Add sample receipts for today if today
            if (dayOffset == 0) {
                val waiters = listOf("አልማዝ", "ሰለሞን", "መሰረት")
                val sampleOrders = listOf(
                    Triple("1", "አልማዝ", 885.50),
                    Triple("3", "አልማዝ", 569.25),
                    Triple("2", "ሰለሞን", 1265.00),
                    Triple("4", "ሰለሞን", 460.00),
                    Triple("5", "መሰረት", 1150.00)
                )

                for ((tbl, waiter, total) in sampleOrders) {
                    val base = total / 1.265
                    val service = base * 0.10
                    val vat = (base + service) * 0.15
                    val time = EthiopianDateUtil.toEthiopianTime(dayMillis)

                    seedReceipts.add(
                        ReceiptEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            timestamp = dayMillis,
                            ethiopianDateKey = ethDate.dateKey,
                            ethiopianDateText = ethDate.formattedDate,
                            ethiopianTimeText = time.formattedTime,
                            ethiopianDayOfWeek = ethDate.dayOfWeekName,
                            restaurantName = "አዲስ ካፌ እና ሬስቶራንት",
                            waiterName = waiter,
                            tableNumber = tbl,
                            baseAmount = base,
                            serviceRate = 0.10,
                            serviceAmount = service,
                            vatRate = 0.15,
                            vatAmount = vat,
                            totalAmount = total,
                            vatOnService = true,
                            includeServiceCharge = true,
                            note = "ጠረጴዛ $tbl ($waiter)",
                            isSettled = waiter == "ሰለሞን" // Some settled, some pending
                        )
                    )
                }
            }
        }

        dao.insertDailySales(seedDailySales)
        if (seedReceipts.isNotEmpty()) {
            dao.insertReceipts(seedReceipts)
        }
    }
}
