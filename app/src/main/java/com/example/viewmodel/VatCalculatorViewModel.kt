package com.example.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.data.EthiopianFoodPresets
import com.example.data.PresetFood
import com.example.data.RestaurantRepository
import com.example.data.local.DailySaleEntity
import com.example.data.local.ReceiptEntity
import com.example.model.CalculationType
import com.example.model.DailySaleSummary
import com.example.model.Language
import com.example.model.OrderItem
import com.example.model.SavedReceipt
import com.example.model.TaxCalculation
import com.example.model.WaiterShiftReport
import com.example.util.EthiopianDateUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class VatUiState(
    val language: Language = Language.AMHARIC, // Exclusively Amharic
    val selectedTab: Int = 0, // 0: Waiter POS, 1: Waiters Sales Collection, 2: Weekly & Daily Sales, 3: Owner & Tax Tools

    // Restaurant & Waiter Info
    val restaurantName: String = "አዲስ ካፌ እና ሬስቶራንት",
    val waiterName: String = "አልማዝ",

    // Standard Ethiopian Tax Settings
    val serviceRate: Double = 0.10, // 10% standard service charge
    val vatRate: Double = 0.15, // 15% standard Ethiopian VAT
    val vatOnService: Boolean = true, // Ethiopian MOR/ERCA rule
    val includeServiceCharge: Boolean = true, // Service charge is optional per user request!

    // --- WAITER POS STATE ---
    val tableNumber: String = "1",
    val waiterEntryMode: Int = 0, // 0: Menu Items Picker, 1: Quick Base Amount Entry
    val waiterQuickAmount: String = "",
    val orderItems: List<OrderItem> = listOf(
        OrderItem(nameEn = "ልዩ ጥብስ", nameAm = "ልዩ ጥብስ", quantity = 1, unitPrice = 500.0),
        OrderItem(nameEn = "ቢራ", nameAm = "ቢራ", quantity = 2, unitPrice = 100.0)
    ),
    val waiterCalculation: TaxCalculation = TaxCalculation(),

    // --- OWNER & TAX TOOLS STATE ---
    val ownerMode: Int = 0, // 0: Menu Price Setter (Base -> Total), 1: Reverse Extract (Total -> Base)
    val ownerBaseInput: String = "500",
    val ownerBaseCalculation: TaxCalculation = TaxCalculation(),
    val ownerTotalInput: String = "632.50",
    val ownerTotalCalculation: TaxCalculation = TaxCalculation(),

    // --- SHIFT SALES & WAITERS COLLECTION LEDGER ---
    val history: List<SavedReceipt> = emptyList(),

    // --- PERSISTENT WEEKLY & DAILY SALES ARCHIVE (AT LEAST A WEEK) ---
    val weeklyDailySales: List<DailySaleSummary> = emptyList(),

    // Backward compatibility
    val splitPeopleCount: Int = 2,
    val tipPercentage: Double = 0.0,
    val customTipAmount: Double = 0.0,
    val isCustomTip: Boolean = false,
    val forwardCalculation: TaxCalculation = TaxCalculation(),
    val reverseCalculation: TaxCalculation = TaxCalculation(),
    val itemizedCalculation: TaxCalculation = TaxCalculation(),

    // UI Message
    val userMessage: String? = null
) {
    // Computed Shift Totals
    val shiftTotalOrders: Int get() = history.size
    val shiftGrossSales: Double get() = history.sumOf { it.totalAmount }
    val shiftVatToRemit: Double get() = history.sumOf { it.vatAmount }
    val shiftStaffServicePool: Double get() = history.sumOf { it.serviceAmount }
    val shiftNetBaseRevenue: Double get() = history.sumOf { it.baseAmount }

    // Handover & Settlement Totals
    val totalSettledCash: Double get() = history.filter { it.isSettled }.sumOf { it.totalAmount }
    val totalPendingCash: Double get() = history.filterNot { it.isSettled }.sumOf { it.totalAmount }

    // Weekly Computed Totals
    val weeklyGrossSales: Double get() = weeklyDailySales.sumOf { it.grossSales }
    val weeklyVatTotal: Double get() = weeklyDailySales.sumOf { it.vatToRemit }
    val weeklyServiceTotal: Double get() = weeklyDailySales.sumOf { it.servicePool }
    val weeklyTotalOrders: Int get() = weeklyDailySales.sumOf { it.totalOrders }

    // Grouped by Waiter for Ethiopian Waiting System
    val waiterShiftReports: List<WaiterShiftReport>
        get() {
            return history.groupBy { it.waiterName.ifBlank { "አስተናጋጅ" } }
                .map { (wName, receipts) ->
                    WaiterShiftReport(
                        waiterName = wName,
                        totalOrders = receipts.size,
                        totalCashToCollect = receipts.sumOf { it.totalAmount },
                        totalBaseRevenue = receipts.sumOf { it.baseAmount },
                        totalVat = receipts.sumOf { it.vatAmount },
                        totalServiceCharge = receipts.sumOf { it.serviceAmount },
                        settledOrders = receipts.count { it.isSettled },
                        pendingCash = receipts.filterNot { it.isSettled }.sumOf { it.totalAmount }
                    )
                }
                .sortedByDescending { it.totalCashToCollect }
        }
}

class VatCalculatorViewModel(
    private val repository: RestaurantRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(VatUiState())
    val uiState: StateFlow<VatUiState> = _uiState.asStateFlow()

    init {
        recalculateWaiterOrder()
        recalculateOwnerBase("500")
        recalculateOwnerTotal("632.50")

        if (repository != null) {
            viewModelScope.launch {
                repository.seedInitialWeeklySalesIfEmpty()
            }

            viewModelScope.launch {
                repository.weeklyDailySales.collect { list ->
                    _uiState.update { current ->
                        current.copy(
                            weeklyDailySales = list.map { it.toSummary() }
                        )
                    }
                }
            }

            viewModelScope.launch {
                repository.allReceipts.collect { list ->
                    _uiState.update { current ->
                        current.copy(
                            history = list.map { it.toSavedReceipt() }
                        )
                    }
                }
            }
        } else {
            // In-memory fallback sample receipts if repository is not injected
            val now = System.currentTimeMillis()
            val ethDate = EthiopianDateUtil.toEthiopianDate(now)
            val ethTime = EthiopianDateUtil.toEthiopianTime(now)
            _uiState.update { current ->
                current.copy(
                    history = listOf(
                        SavedReceipt(
                            timestamp = now,
                            ethiopianDateKey = ethDate.dateKey,
                            ethiopianDateText = ethDate.formattedDate,
                            ethiopianTimeText = ethTime.formattedTime,
                            ethiopianDayOfWeek = ethDate.dayOfWeekName,
                            restaurantName = "አዲስ ካፌ እና ሬስቶራንት",
                            waiterName = "አልማዝ",
                            tableNumber = "1",
                            baseAmount = 700.0,
                            serviceRate = 0.10,
                            serviceAmount = 70.0,
                            vatRate = 0.15,
                            vatAmount = 115.50,
                            totalAmount = 885.50,
                            vatOnService = true,
                            includeServiceCharge = true,
                            note = "ጠረጴዛ 1",
                            isSettled = false
                        ),
                        SavedReceipt(
                            timestamp = now - 1800000,
                            ethiopianDateKey = ethDate.dateKey,
                            ethiopianDateText = ethDate.formattedDate,
                            ethiopianTimeText = ethTime.formattedTime,
                            ethiopianDayOfWeek = ethDate.dayOfWeekName,
                            restaurantName = "አዲስ ካፌ እና ሬስቶራንት",
                            waiterName = "ሰለሞን",
                            tableNumber = "2",
                            baseAmount = 1000.0,
                            serviceRate = 0.10,
                            serviceAmount = 100.0,
                            vatRate = 0.15,
                            vatAmount = 165.00,
                            totalAmount = 1265.00,
                            vatOnService = true,
                            includeServiceCharge = true,
                            note = "ጠረጴዛ 2",
                            isSettled = true
                        )
                    )
                )
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // ==========================================
    // RESTAURANT & WAITER CONFIG
    // ==========================================
    fun setRestaurantName(name: String) {
        _uiState.update { it.copy(restaurantName = name) }
    }

    fun setWaiterName(name: String) {
        _uiState.update { it.copy(waiterName = name) }
    }

    // ==========================================
    // OPTIONAL SERVICE CHARGE & TAX SETTINGS
    // ==========================================
    fun setIncludeServiceCharge(include: Boolean) {
        _uiState.update { it.copy(includeServiceCharge = include) }
        refreshAllCalculations()
    }

    fun setServiceRate(rate: Double) {
        _uiState.update { it.copy(serviceRate = rate) }
        refreshAllCalculations()
    }

    fun setVatOnService(enabled: Boolean) {
        _uiState.update { it.copy(vatOnService = enabled) }
        refreshAllCalculations()
    }

    private fun refreshAllCalculations() {
        recalculateWaiterOrder()
        val s = _uiState.value
        recalculateOwnerBase(s.ownerBaseInput)
        recalculateOwnerTotal(s.ownerTotalInput)
    }

    // ==========================================
    // WAITER POS FUNCTIONS
    // ==========================================
    fun setTableNumber(table: String) {
        val sanitized = table.filter { it.isLetterOrDigit() || it == ' ' }.take(10)
        _uiState.update { it.copy(tableNumber = sanitized) }
    }

    fun setWaiterEntryMode(mode: Int) {
        _uiState.update { it.copy(waiterEntryMode = mode) }
        recalculateWaiterOrder()
    }

    fun onWaiterQuickAmountChanged(input: String) {
        val sanitized = sanitizeNumericInput(input)
        _uiState.update { it.copy(waiterQuickAmount = sanitized) }
        recalculateWaiterOrder()
    }

    fun addWaiterQuickAmount(add: Double) {
        val current = _uiState.value.waiterQuickAmount.toDoubleOrNull() ?: 0.0
        val newAmount = current + add
        val str = if (newAmount % 1.0 == 0.0) newAmount.toLong().toString() else "%.2f".format(Locale.US, newAmount)
        onWaiterQuickAmountChanged(str)
    }

    fun addWaiterItem(preset: PresetFood) {
        val currentItems = _uiState.value.orderItems
        val existingIndex = currentItems.indexOfFirst { it.nameAm == preset.nameAm }
        val updated = if (existingIndex >= 0) {
            currentItems.mapIndexed { idx, item ->
                if (idx == existingIndex) item.copy(quantity = item.quantity + 1) else item
            }
        } else {
            currentItems + OrderItem(
                nameEn = preset.nameAm,
                nameAm = preset.nameAm,
                quantity = 1,
                unitPrice = preset.defaultPrice
            )
        }
        _uiState.update { it.copy(orderItems = updated) }
        recalculateWaiterOrder()
    }

    fun addCustomOrderItem(name: String, price: Double, qty: Int = 1) {
        if (name.isBlank() || price <= 0.0) return
        val newItem = OrderItem(
            nameEn = name.trim(),
            nameAm = name.trim(),
            quantity = qty.coerceAtLeast(1),
            unitPrice = price
        )
        _uiState.update { it.copy(orderItems = it.orderItems + newItem) }
        recalculateWaiterOrder()
    }

    fun updateOrderItemPrice(itemId: String, newPrice: Double) {
        if (newPrice < 0.0) return
        val updated = _uiState.value.orderItems.map { item ->
            if (item.id == itemId) item.copy(unitPrice = newPrice) else item
        }
        _uiState.update { it.copy(orderItems = updated) }
        recalculateWaiterOrder()
    }

    fun updateOrderItemQty(itemId: String, delta: Int) {
        val updated = _uiState.value.orderItems.mapNotNull { item ->
            if (item.id == itemId) {
                val newQty = item.quantity + delta
                if (newQty <= 0) null else item.copy(quantity = newQty)
            } else {
                item
            }
        }
        _uiState.update { it.copy(orderItems = updated) }
        recalculateWaiterOrder()
    }

    fun removeOrderItem(itemId: String) {
        val updated = _uiState.value.orderItems.filterNot { it.id == itemId }
        _uiState.update { it.copy(orderItems = updated) }
        recalculateWaiterOrder()
    }

    fun clearWaiterOrder() {
        _uiState.update {
            it.copy(
                orderItems = emptyList(),
                waiterQuickAmount = ""
            )
        }
        recalculateWaiterOrder()
    }

    private fun recalculateWaiterOrder() {
        val s = _uiState.value
        val base = if (s.waiterEntryMode == 0) {
            s.orderItems.sumOf { it.totalPrice }
        } else {
            s.waiterQuickAmount.toDoubleOrNull() ?: 0.0
        }

        val serviceAmount = if (s.includeServiceCharge) base * s.serviceRate else 0.0
        val vatAmount = if (s.includeServiceCharge && s.vatOnService) {
            (base + serviceAmount) * s.vatRate
        } else {
            base * s.vatRate
        }
        val total = base + serviceAmount + vatAmount

        val calc = TaxCalculation(
            baseAmount = base,
            serviceRate = if (s.includeServiceCharge) s.serviceRate else 0.0,
            serviceAmount = serviceAmount,
            vatRate = s.vatRate,
            vatAmount = vatAmount,
            totalAmount = total,
            vatOnService = s.vatOnService,
            includeServiceCharge = s.includeServiceCharge
        )
        _uiState.update {
            it.copy(
                waiterCalculation = calc,
                forwardCalculation = calc,
                itemizedCalculation = calc
            )
        }
    }

    // ==========================================
    // OWNER & TAX TOOLS FUNCTIONS
    // ==========================================
    fun setOwnerMode(mode: Int) {
        _uiState.update { it.copy(ownerMode = mode) }
    }

    fun onOwnerBaseInputChanged(input: String) {
        val sanitized = sanitizeNumericInput(input)
        _uiState.update { it.copy(ownerBaseInput = sanitized) }
        recalculateOwnerBase(sanitized)
    }

    private fun recalculateOwnerBase(input: String) {
        val s = _uiState.value
        val base = input.toDoubleOrNull() ?: 0.0
        val serviceAmount = if (s.includeServiceCharge) base * s.serviceRate else 0.0
        val vatAmount = if (s.includeServiceCharge && s.vatOnService) {
            (base + serviceAmount) * s.vatRate
        } else {
            base * s.vatRate
        }
        val total = base + serviceAmount + vatAmount

        val calc = TaxCalculation(
            baseAmount = base,
            serviceRate = if (s.includeServiceCharge) s.serviceRate else 0.0,
            serviceAmount = serviceAmount,
            vatRate = s.vatRate,
            vatAmount = vatAmount,
            totalAmount = total,
            vatOnService = s.vatOnService,
            includeServiceCharge = s.includeServiceCharge
        )
        _uiState.update {
            it.copy(
                ownerBaseCalculation = calc,
                forwardCalculation = calc
            )
        }
    }

    fun onOwnerTotalInputChanged(input: String) {
        val sanitized = sanitizeNumericInput(input)
        _uiState.update { it.copy(ownerTotalInput = sanitized) }
        recalculateOwnerTotal(sanitized)
    }

    private fun recalculateOwnerTotal(input: String) {
        val s = _uiState.value
        val total = input.toDoubleOrNull() ?: 0.0
        val calc = if (total <= 0.0) {
            TaxCalculation(
                serviceRate = if (s.includeServiceCharge) s.serviceRate else 0.0,
                vatRate = s.vatRate,
                vatOnService = s.vatOnService,
                includeServiceCharge = s.includeServiceCharge
            )
        } else if (s.includeServiceCharge && s.vatOnService) {
            val taxableBase = total / (1.0 + s.vatRate)
            val vatAmount = total - taxableBase
            val base = taxableBase / (1.0 + s.serviceRate)
            val serviceAmount = taxableBase - base
            TaxCalculation(
                baseAmount = base,
                serviceRate = s.serviceRate,
                serviceAmount = serviceAmount,
                vatRate = s.vatRate,
                vatAmount = vatAmount,
                totalAmount = total,
                vatOnService = s.vatOnService,
                includeServiceCharge = true
            )
        } else if (s.includeServiceCharge) {
            val divisor = 1.0 + s.serviceRate + s.vatRate
            val base = total / divisor
            val serviceAmount = base * s.serviceRate
            val vatAmount = base * s.vatRate
            TaxCalculation(
                baseAmount = base,
                serviceRate = s.serviceRate,
                serviceAmount = serviceAmount,
                vatRate = s.vatRate,
                vatAmount = vatAmount,
                totalAmount = total,
                vatOnService = false,
                includeServiceCharge = true
            )
        } else {
            // Service charge disabled: total = base + 15% VAT
            val base = total / (1.0 + s.vatRate)
            val vatAmount = total - base
            TaxCalculation(
                baseAmount = base,
                serviceRate = 0.0,
                serviceAmount = 0.0,
                vatRate = s.vatRate,
                vatAmount = vatAmount,
                totalAmount = total,
                vatOnService = false,
                includeServiceCharge = false
            )
        }
        _uiState.update {
            it.copy(
                ownerTotalCalculation = calc,
                reverseCalculation = calc
            )
        }
    }

    // ==========================================
    // RECEIPT SAVING & SHIFT LOGGING (ROOM & ETHIOPIAN CALENDAR)
    // ==========================================
    fun saveCurrentWaiterReceipt() {
        val calc = _uiState.value.waiterCalculation
        if (calc.totalAmount <= 0.0) return

        val s = _uiState.value
        val tableNote = "ጠረጴዛ ${s.tableNumber} (${s.waiterName})"
        val now = System.currentTimeMillis()
        val ethDate = EthiopianDateUtil.toEthiopianDate(now)
        val ethTime = EthiopianDateUtil.toEthiopianTime(now)

        val newRecord = SavedReceipt(
            timestamp = now,
            ethiopianDateKey = ethDate.dateKey,
            ethiopianDateText = ethDate.formattedDate,
            ethiopianTimeText = ethTime.formattedTime,
            ethiopianDayOfWeek = ethDate.dayOfWeekName,
            calculationType = CalculationType.ITEMIZED,
            restaurantName = s.restaurantName,
            waiterName = s.waiterName,
            tableNumber = s.tableNumber,
            baseAmount = calc.baseAmount,
            serviceRate = calc.serviceRate,
            serviceAmount = calc.serviceAmount,
            vatRate = calc.vatRate,
            vatAmount = calc.vatAmount,
            totalAmount = calc.totalAmount,
            vatOnService = calc.vatOnService,
            includeServiceCharge = calc.includeServiceCharge,
            note = tableNote,
            isSettled = false
        )

        viewModelScope.launch {
            repository?.saveReceipt(newRecord.toEntity())
        }

        _uiState.update { current ->
            current.copy(
                history = listOf(newRecord) + current.history.take(99),
                userMessage = AppStrings.get(StringKey.SAVED_SUCCESS)
            )
        }
    }

    // ==========================================
    // ETHIOPIAN WAITING SYSTEM: SETTLEMENT & HANDOVER
    // ==========================================
    fun settleWaiterSales(waiterName: String) {
        viewModelScope.launch {
            repository?.settleWaiterSales(waiterName)
        }
        _uiState.update { current ->
            val updatedHistory = current.history.map { receipt ->
                if (receipt.waiterName.equals(waiterName, ignoreCase = true)) {
                    receipt.copy(isSettled = true)
                } else {
                    receipt
                }
            }
            current.copy(
                history = updatedHistory,
                userMessage = "የ${waiterName} የጥሬ ገንዘብ ሂሳብ ተረክቧል ✓"
            )
        }
    }

    fun toggleReceiptSettled(receiptId: String) {
        val targetReceipt = _uiState.value.history.find { it.id == receiptId }
        val newSettled = !(targetReceipt?.isSettled ?: false)

        viewModelScope.launch {
            repository?.toggleReceiptSettled(receiptId, newSettled)
        }

        _uiState.update { current ->
            val updated = current.history.map {
                if (it.id == receiptId) it.copy(isSettled = newSettled) else it
            }
            current.copy(history = updated)
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository?.deleteReceipt(id)
        }
        _uiState.update { current ->
            current.copy(history = current.history.filterNot { it.id == id })
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository?.clearAllReceipts()
        }
        _uiState.update { it.copy(history = emptyList(), userMessage = "የቀን መዝገብ ጸድቷል") }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // ==========================================
    // DAILY SALES ARCHIVE (SAVE FOR AT LEAST A WEEK)
    // ==========================================
    fun saveDailySaleForCurrentDay() {
        val s = _uiState.value
        val now = System.currentTimeMillis()
        val ethDate = EthiopianDateUtil.toEthiopianDate(now)

        val dailySummary = DailySaleSummary(
            dateKey = ethDate.dateKey,
            ethiopianDateText = ethDate.formattedDate,
            dayOfWeek = ethDate.dayOfWeekName,
            timestamp = now,
            totalOrders = s.shiftTotalOrders,
            grossSales = s.shiftGrossSales,
            baseRevenue = s.shiftNetBaseRevenue,
            vatToRemit = s.shiftVatToRemit,
            servicePool = s.shiftStaffServicePool,
            settledCash = s.totalSettledCash,
            pendingCash = s.totalPendingCash,
            isClosed = true,
            notes = "የ${ethDate.dayOfWeekName} የተጠናቀቀ የቀን ሒሳብ"
        )

        viewModelScope.launch {
            repository?.saveDailySale(dailySummary.toEntity())
        }

        _uiState.update { current ->
            val existing = current.weeklyDailySales.filterNot { it.dateKey == dailySummary.dateKey }
            current.copy(
                weeklyDailySales = listOf(dailySummary) + existing,
                userMessage = AppStrings.get(StringKey.DAILY_SALE_SAVED_SUCCESS)
            )
        }
    }

    // ==========================================
    // SHARE & PRINT RECEIPT (100% AMHARIC WITH ETHIOPIAN CALENDAR & TIME)
    // ==========================================
    fun shareWaiterReceipt(context: Context) {
        val s = _uiState.value
        val text = buildWaiterReceiptText(s)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }

    fun copyWaiterReceipt(context: Context) {
        val s = _uiState.value
        val text = buildWaiterReceiptText(s)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Restaurant Receipt", text)
        clipboard.setPrimaryClip(clip)
        _uiState.update {
            it.copy(userMessage = AppStrings.get(StringKey.COPIED_SUCCESS))
        }
    }

    fun shareWaiterShiftReport(context: Context, waiterName: String) {
        val s = _uiState.value
        val currency = "ብር"
        val ethDate = EthiopianDateUtil.toEthiopianDate()
        val ethTime = EthiopianDateUtil.toEthiopianTime()

        val waiterReceipts = s.history.filter { it.waiterName.equals(waiterName, ignoreCase = true) }
        val totalGross = waiterReceipts.sumOf { it.totalAmount }
        val totalBase = waiterReceipts.sumOf { it.baseAmount }
        val totalService = waiterReceipts.sumOf { it.serviceAmount }
        val totalVat = waiterReceipts.sumOf { it.vatAmount }
        val isAllSettled = waiterReceipts.isNotEmpty() && waiterReceipts.all { it.isSettled }

        val text = buildString {
            appendLine("📋 የአስተናጋጅ የቀን ሽያጭ መረከቢያ (Shift Handover)")
            appendLine("🏢 ${s.restaurantName}")
            appendLine("👤 አስተናጋጅ: $waiterName")
            appendLine("📅 ቀን: ${ethDate.fullDateWithDay}")
            appendLine("⏰ ሰዓት: ${ethTime.formattedTime} (የኢትዮጵያ ሰዓት)")
            appendLine("----------------------------------------")
            appendLine("የተስተናገዱ ጠረጴዛዎች: ${waiterReceipts.size}")
            appendLine("💰 ከአስተናጋጁ የሚሰበሰብ ጥሬ ገንዘብ: ${formatMoney(totalGross)} $currency")
            appendLine("----------------------------------------")
            appendLine("ዝርዝር ክፍፍል:")
            appendLine(" • የምግብ/መጠጥ መነሻ ገቢ: ${formatMoney(totalBase)} $currency")
            appendLine(" • 15% ተጨማሪ እሴት ታክስ: ${formatMoney(totalVat)} $currency")
            appendLine(" • የአገልግሎት ክፍያ (ሰርቪስ): ${formatMoney(totalService)} $currency")
            appendLine("----------------------------------------")
            appendLine("የክፍያ ሁኔታ: ${if (isAllSettled) "ተረክቧል ✓" else "ያልተረከበ (ይጠየቃል)"}")
            appendLine("========================================")
            appendLine("ተረካቢ (ካሸር): ____________________")
            appendLine("አስረካቢ (አስተናጋጅ): $waiterName")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }

    fun shareWeeklyReport(context: Context) {
        val s = _uiState.value
        val currency = "ብር"
        val ethDate = EthiopianDateUtil.toEthiopianDate()
        val ethTime = EthiopianDateUtil.toEthiopianTime()

        val text = buildString {
            appendLine("📊 የሳምንቱ የቀን ሽያጮች እና ታክስ ማጠቃለያ ሪፖርት")
            appendLine("🏢 ${s.restaurantName}")
            appendLine("📅 ዘመን: ${ethDate.fullDateWithDay} | ⏰ ${ethTime.formattedTime}")
            appendLine("----------------------------------------")
            appendLine("💰 የሳምንቱ ጠቅላላ ሽያጭ: ${formatMoney(s.weeklyGrossSales)} $currency")
            appendLine("🏛️ የሳምንቱ 15% ቫት (ለመንግስት): ${formatMoney(s.weeklyVatTotal)} $currency")
            appendLine("👥 የሳምንቱ ሰርቪስ ገንዳ: ${formatMoney(s.weeklyServiceTotal)} $currency")
            appendLine("🧾 የሳምንቱ ጠቅላላ ደረሰኞች: ${s.weeklyTotalOrders} ትዕዛዞች")
            appendLine("========================================")
            appendLine("የእያንዳንዱ ቀን ዝርዝር (ያለፉት 7 ቀናት):")
            for (day in s.weeklyDailySales) {
                appendLine("• ${day.dayOfWeek} (${day.ethiopianDateText}):")
                appendLine("   - ሽያጭ: ${formatMoney(day.grossSales)} $currency | 15% ቫት: ${formatMoney(day.vatToRemit)} $currency")
                appendLine("   - ሰርቪስ: ${formatMoney(day.servicePool)} $currency | ደረሰኞች: ${day.totalOrders}")
            }
            appendLine("========================================")
            appendLine("ሪፖርት ያዘጋጀው: የኢትዮጵያ ሬስቶራንት ቫት እና ሂሳብ ማኔጀር")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }

    fun shareDailySaleReport(context: Context, dateKey: String) {
        val s = _uiState.value
        val day = s.weeklyDailySales.find { it.dateKey == dateKey } ?: return
        val currency = "ብር"

        val text = buildString {
            appendLine("📋 የቀን ሽያጭ እና ታክስ ሪፖርት")
            appendLine("🏢 ${s.restaurantName}")
            appendLine("📅 ቀን: ${day.dayOfWeek}፣ ${day.ethiopianDateText}")
            appendLine("----------------------------------------")
            appendLine("💰 ጠቅላላ ሽያጭ: ${formatMoney(day.grossSales)} $currency")
            appendLine("🍲 የምግብ/መጠጥ መነሻ ገቢ: ${formatMoney(day.baseRevenue)} $currency")
            appendLine("🏛️ 15% ተጨማሪ እሴት ታክስ (ቫት): ${formatMoney(day.vatToRemit)} $currency")
            appendLine("👥 የአገልግሎት ክፍያ (ሰርቪስ): ${formatMoney(day.servicePool)} $currency")
            appendLine("----------------------------------------")
            appendLine("💵 የተረከበ ጥሬ ገንዘብ: ${formatMoney(day.settledCash)} $currency")
            appendLine("⏳ ያልተረከበ / በእጅ ያለ: ${formatMoney(day.pendingCash)} $currency")
            appendLine("🧾 የተስተናገዱ ደረሰኞች: ${day.totalOrders}")
            appendLine("📌 ሁኔታ: ${if (day.isClosed) "የተዘጋ / የተጠናቀቀ ✓" else "ክፍት ፈረቃ"}")
            appendLine("========================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun buildWaiterReceiptText(s: VatUiState): String {
        val currency = "ብር"
        val ethDate = EthiopianDateUtil.toEthiopianDate()
        val ethTime = EthiopianDateUtil.toEthiopianTime()
        val calc = s.waiterCalculation

        return buildString {
            appendLine("🍽️ ${s.restaurantName}")
            appendLine("👤 አስተናጋጅ: ${s.waiterName}")
            appendLine("ጠረጴዛ ቁጥር: ${s.tableNumber}")
            appendLine("📅 ቀን: ${ethDate.fullDateWithDay}")
            appendLine("⏰ ሰዓት: ${ethTime.formattedTime} (የኢትዮጵያ ሰዓት)")
            appendLine("----------------------------------------")
            if (s.waiterEntryMode == 0 && s.orderItems.isNotEmpty()) {
                appendLine("የትዕዛዝ ዝርዝር:")
                for (item in s.orderItems) {
                    appendLine(" • ${item.nameAm} x${item.quantity} = ${formatMoney(item.totalPrice)} $currency")
                }
                appendLine("----------------------------------------")
            }
            appendLine("የምግብ/መጠጥ መነሻ ዋጋ: ${formatMoney(calc.baseAmount)} $currency")
            if (calc.includeServiceCharge) {
                appendLine("10% የአገልግሎት ክፍያ (ሰርቪስ): ${formatMoney(calc.serviceAmount)} $currency")
            } else {
                appendLine("የአገልግሎት ክፍያ: 0.00 $currency (አልተካተተም)")
            }
            appendLine("15% ተጨማሪ እሴት ታክስ (ቫት): ${formatMoney(calc.vatAmount)} $currency")
            appendLine("========================================")
            appendLine("ጠቅላላ የሚከፈል ሂሳብ: ${formatMoney(calc.totalAmount)} $currency")
            appendLine("========================================")
            appendLine("እናመሰግናለን! እንደገና ይጎብኙን!")
        }
    }

    // Compatibility methods
    fun copyReceiptToClipboard(context: Context, calc: TaxCalculation) {
        copyWaiterReceipt(context)
    }

    fun shareReceipt(context: Context, calc: TaxCalculation) {
        shareWaiterReceipt(context)
    }

    fun onForwardBaseChanged(input: String) = onOwnerBaseInputChanged(input)
    fun onReverseTotalChanged(input: String) = onOwnerTotalInputChanged(input)
    fun setSplitPeopleCount(count: Int) { _uiState.update { it.copy(splitPeopleCount = count) } }
    fun setTipPercentage(pct: Double) { _uiState.update { it.copy(tipPercentage = pct) } }

    private fun sanitizeNumericInput(input: String): String {
        val filtered = input.filter { it.isDigit() || it == '.' }
        val dotIndex = filtered.indexOf('.')
        return if (dotIndex != -1) {
            val beforeDot = filtered.substring(0, dotIndex + 1)
            val afterDot = filtered.substring(dotIndex + 1).replace(".", "")
            beforeDot + afterDot
        } else {
            filtered
        }
    }

    companion object {
        fun formatMoney(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            return formatter.format(amount)
        }
    }
}

fun SavedReceipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    timestamp = timestamp,
    ethiopianDateKey = ethiopianDateKey.ifBlank { EthiopianDateUtil.toEthiopianDate(timestamp).dateKey },
    ethiopianDateText = ethiopianDateText.ifBlank { EthiopianDateUtil.toEthiopianDate(timestamp).formattedDate },
    ethiopianTimeText = ethiopianTimeText.ifBlank { EthiopianDateUtil.toEthiopianTime(timestamp).formattedTime },
    ethiopianDayOfWeek = ethiopianDayOfWeek.ifBlank { EthiopianDateUtil.toEthiopianDate(timestamp).dayOfWeekName },
    restaurantName = restaurantName,
    waiterName = waiterName,
    tableNumber = tableNumber,
    baseAmount = baseAmount,
    serviceRate = serviceRate,
    serviceAmount = serviceAmount,
    vatRate = vatRate,
    vatAmount = vatAmount,
    totalAmount = totalAmount,
    vatOnService = vatOnService,
    includeServiceCharge = includeServiceCharge,
    note = note,
    isSettled = isSettled
)

fun ReceiptEntity.toSavedReceipt(): SavedReceipt = SavedReceipt(
    id = id,
    timestamp = timestamp,
    ethiopianDateKey = ethiopianDateKey,
    ethiopianDateText = ethiopianDateText,
    ethiopianTimeText = ethiopianTimeText,
    ethiopianDayOfWeek = ethiopianDayOfWeek,
    calculationType = CalculationType.ITEMIZED,
    restaurantName = restaurantName,
    waiterName = waiterName,
    tableNumber = tableNumber,
    baseAmount = baseAmount,
    serviceRate = serviceRate,
    serviceAmount = serviceAmount,
    vatRate = vatRate,
    vatAmount = vatAmount,
    totalAmount = totalAmount,
    vatOnService = vatOnService,
    includeServiceCharge = includeServiceCharge,
    note = note,
    isSettled = isSettled
)

fun DailySaleSummary.toEntity(): DailySaleEntity = DailySaleEntity(
    dateKey = dateKey,
    ethiopianDateText = ethiopianDateText,
    dayOfWeek = dayOfWeek,
    timestamp = timestamp,
    totalOrders = totalOrders,
    grossSales = grossSales,
    baseRevenue = baseRevenue,
    vatToRemit = vatToRemit,
    servicePool = servicePool,
    settledCash = settledCash,
    pendingCash = pendingCash,
    isClosed = isClosed,
    notes = notes
)

fun DailySaleEntity.toSummary(): DailySaleSummary = DailySaleSummary(
    dateKey = dateKey,
    ethiopianDateText = ethiopianDateText,
    dayOfWeek = dayOfWeek,
    timestamp = timestamp,
    totalOrders = totalOrders,
    grossSales = grossSales,
    baseRevenue = baseRevenue,
    vatToRemit = vatToRemit,
    servicePool = servicePool,
    settledCash = settledCash,
    pendingCash = pendingCash,
    isClosed = isClosed,
    notes = notes
)

