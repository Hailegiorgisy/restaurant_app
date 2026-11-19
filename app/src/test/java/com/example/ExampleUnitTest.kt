package com.example

import com.example.model.SavedReceipt
import com.example.model.TaxCalculation
import com.example.viewmodel.VatCalculatorViewModel
import com.example.viewmodel.VatUiState
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVatCalculationWithServiceCharge() {
        val base = 1000.0
        val serviceRate = 0.10
        val vatRate = 0.15
        val serviceAmount = base * serviceRate // 100.0
        val vatAmount = (base + serviceAmount) * vatRate // 165.0
        val total = base + serviceAmount + vatAmount // 1265.0

        val calc = TaxCalculation(
            baseAmount = base,
            serviceRate = serviceRate,
            serviceAmount = serviceAmount,
            vatRate = vatRate,
            vatAmount = vatAmount,
            totalAmount = total,
            vatOnService = true,
            includeServiceCharge = true
        )

        assertEquals(100.0, calc.serviceAmount, 0.001)
        assertEquals(165.0, calc.vatAmount, 0.001)
        assertEquals(1265.0, calc.totalAmount, 0.001)
    }

    @Test
    fun testVatCalculationWithoutServiceCharge() {
        val base = 1000.0
        val serviceRate = 0.0
        val vatRate = 0.15
        val serviceAmount = 0.0
        val vatAmount = base * vatRate // 150.0
        val total = base + vatAmount // 1150.0

        val calc = TaxCalculation(
            baseAmount = base,
            serviceRate = serviceRate,
            serviceAmount = serviceAmount,
            vatRate = vatRate,
            vatAmount = vatAmount,
            totalAmount = total,
            vatOnService = false,
            includeServiceCharge = false
        )

        assertEquals(0.0, calc.serviceAmount, 0.001)
        assertEquals(150.0, calc.vatAmount, 0.001)
        assertEquals(1150.0, calc.totalAmount, 0.001)
    }

    @Test
    fun testWaiterShiftReportsAggregation() {
        val state = VatUiState(
            history = listOf(
                SavedReceipt(waiterName = "አልማዝ", totalAmount = 500.0, isSettled = false),
                SavedReceipt(waiterName = "አልማዝ", totalAmount = 300.0, isSettled = true),
                SavedReceipt(waiterName = "ሰለሞን", totalAmount = 700.0, isSettled = false)
            )
        )

        val reports = state.waiterShiftReports
        assertEquals(2, reports.size)

        val almazReport = reports.first { it.waiterName == "አልማዝ" }
        assertEquals(800.0, almazReport.totalCashToCollect, 0.001)
        assertEquals(500.0, almazReport.pendingCash, 0.001)
        assertEquals(1, almazReport.settledOrders)

        val solomonReport = reports.first { it.waiterName == "ሰለሞን" }
        assertEquals(700.0, solomonReport.totalCashToCollect, 0.001)
        assertEquals(700.0, solomonReport.pendingCash, 0.001)
    }
}

