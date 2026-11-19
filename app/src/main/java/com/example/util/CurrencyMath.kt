package com.example.util

import com.example.model.TaxCalculation
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Exact decimal currency and Ethiopian VAT/service-charge calculations
 * using BigDecimal with HALF_UP rounding to avoid Double precision drift.
 */
object CurrencyMath {
    private val ONE = BigDecimal.ONE
    private val SCALE = 2
    private val ROUNDING = RoundingMode.HALF_UP

    fun round(value: Double): Double =
        BigDecimal.valueOf(value).setScale(SCALE, ROUNDING).toDouble()

    fun calculateForward(
        baseAmount: Double,
        serviceRate: Double = 0.10,
        vatRate: Double = 0.15,
        includesServiceCharge: Boolean = true,
        vatOnService: Boolean = true
    ): TaxCalculation {
        val base = BigDecimal.valueOf(baseAmount).max(BigDecimal.ZERO)
        val sRate = if (includesServiceCharge) BigDecimal.valueOf(serviceRate) else BigDecimal.ZERO
        val vRate = BigDecimal.valueOf(vatRate)

        val service = base.multiply(sRate).setScale(SCALE, ROUNDING)
        val taxable = if (vatOnService) base.add(service) else base
        val vat = taxable.multiply(vRate).setScale(SCALE, ROUNDING)
        val total = base.add(service).add(vat).setScale(SCALE, ROUNDING)

        return TaxCalculation(
            baseAmount = base.setScale(SCALE, ROUNDING).toDouble(),
            serviceRate = serviceRate,
            serviceAmount = service.toDouble(),
            vatRate = vatRate,
            vatAmount = vat.toDouble(),
            totalAmount = total.toDouble(),
            vatOnService = vatOnService,
            includesServiceCharge = includesServiceCharge
        )
    }

    fun calculateReverse(
        totalAmount: Double,
        serviceRate: Double = 0.10,
        vatRate: Double = 0.15,
        includesServiceCharge: Boolean = true,
        vatOnService: Boolean = true
    ): TaxCalculation {
        val total = BigDecimal.valueOf(totalAmount).max(BigDecimal.ZERO)
        val sRate = if (includesServiceCharge) BigDecimal.valueOf(serviceRate) else BigDecimal.ZERO
        val vRate = BigDecimal.valueOf(vatRate)

        val divisor = if (vatOnService) {
            ONE.add(sRate).multiply(ONE.add(vRate))
        } else {
            ONE.add(sRate).add(vRate)
        }

        val base = if (divisor > BigDecimal.ZERO) {
            total.divide(divisor, SCALE, ROUNDING)
        } else {
            BigDecimal.ZERO.setScale(SCALE, ROUNDING)
        }

        return calculateForward(
            baseAmount = base.toDouble(),
            serviceRate = serviceRate,
            vatRate = vatRate,
            includesServiceCharge = includesServiceCharge,
            vatOnService = vatOnService
        )
    }
}
