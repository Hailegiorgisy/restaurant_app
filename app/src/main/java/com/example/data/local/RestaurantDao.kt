package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // --- RECEIPTS QUERIES ---
    @Query("SELECT * FROM receipts ORDER BY timestamp DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE ethiopianDateKey = :dateKey ORDER BY timestamp DESC")
    fun getReceiptsForDate(dateKey: String): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipts(receipts: List<ReceiptEntity>)

    @Query("UPDATE receipts SET isSettled = :isSettled WHERE id = :id")
    suspend fun updateReceiptSettled(id: String, isSettled: Boolean)

    @Query("UPDATE receipts SET isSettled = 1 WHERE waiterName = :waiterName")
    suspend fun settleAllForWaiter(waiterName: String)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: String)

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()

    // --- DAILY SALES ARCHIVE (AT LEAST A WEEK OF DAILY SALES) ---
    @Query("SELECT * FROM daily_sales ORDER BY timestamp DESC")
    fun getAllDailySales(): Flow<List<DailySaleEntity>>

    @Query("SELECT * FROM daily_sales ORDER BY timestamp DESC LIMIT 7")
    fun getWeeklyDailySales(): Flow<List<DailySaleEntity>>

    @Query("SELECT * FROM daily_sales WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailySale(dateKey: String): DailySaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySale(sale: DailySaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySales(sales: List<DailySaleEntity>)

    @Query("DELETE FROM daily_sales WHERE dateKey = :dateKey")
    suspend fun deleteDailySale(dateKey: String)
}
