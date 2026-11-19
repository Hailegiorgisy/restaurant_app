package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.model.CalculationType
import com.example.model.Language
import com.example.model.TaxCalculation
import com.example.util.EthiopianDateUtil
import com.example.viewmodel.VatCalculatorViewModel

@Composable
fun ReceiptCard(
    calculation: TaxCalculation,
    type: CalculationType,
    language: Language = Language.AMHARIC,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    customTitle: String? = null
) {
    val currency = "ብር"
    var savedClicked by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("receipt_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Receipt Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customTitle ?: AppStrings.get(StringKey.RECEIPT_TITLE),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "የኢትዮጵያ ፌዴራላዊ የግብር ስሌት",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Calculation Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = when (type) {
                            CalculationType.FORWARD -> "ከመነሻ"
                            CalculationType.REVERSE -> "ከጠቅላላ"
                            CalculationType.ITEMIZED -> "ዝርዝር"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dotted Separator
            DottedLineDivider()

            Spacer(modifier = Modifier.height(10.dp))

            // Ethiopian Date and Time Stamp
            val ethDate = remember { EthiopianDateUtil.toEthiopianDate() }
            val ethTime = remember { EthiopianDateUtil.toEthiopianTime() }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ቀን: ${ethDate.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ሰዓት: ${ethTime.formattedTime}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Base Price (መነሻ ዋጋ)
            ReceiptRow(
                label = AppStrings.get(StringKey.BASE_PRICE),
                amount = calculation.baseAmount,
                currency = currency,
                isHighlight = type == CalculationType.REVERSE
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Service Charge (የአገልግሎት ክፍያ)
            if (calculation.includeServiceCharge && calculation.serviceAmount > 0.0) {
                val servicePercent = (calculation.serviceRate * 100).toInt()
                ReceiptRow(
                    label = "${AppStrings.get(StringKey.SERVICE_CHARGE)} ($servicePercent%)",
                    amount = calculation.serviceAmount,
                    currency = currency
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = AppStrings.get(StringKey.SERVICE_CHARGE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "አልተካተተም (0.00 $currency)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: VAT 15% (የተጨማሪ እሴት ታክስ)
            val vatPercent = (calculation.vatRate * 100).toInt()
            ReceiptRow(
                label = "${AppStrings.get(StringKey.VAT_15)} ($vatPercent%)",
                amount = calculation.vatAmount,
                currency = currency,
                isVat = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dotted Line
            DottedLineDivider()

            Spacer(modifier = Modifier.height(14.dp))

            // Total Payable (ጠቅላላ ሂሳብ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = AppStrings.get(StringKey.TOTAL_BILL),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (calculation.vatOnService) {
                            "ቫት በመነሻ + አገልግሎት ላይ ተሰልቷል"
                        } else {
                            "ቫት በመነሻ ዋጋ ላይ ብቻ ተሰልቷል"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = VatCalculatorViewModel.formatMoney(calculation.totalAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons (Copy, Share, Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_receipt_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.get(StringKey.COPY_RECEIPT),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_receipt_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.get(StringKey.SHARE),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                FilledTonalButton(
                    onClick = {
                        onSave()
                        savedClicked = true
                    },
                    modifier = Modifier.testTag("save_receipt_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (savedClicked) {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    }
                ) {
                    Icon(
                        imageVector = if (savedClicked) Icons.Default.Check else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    amount: Double,
    currency: String,
    isHighlight: Boolean = false,
    isVat: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isVat) {
                MaterialTheme.colorScheme.error
            } else if (isHighlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Text(
            text = "${VatCalculatorViewModel.formatMoney(amount)} $currency",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isVat) {
                MaterialTheme.colorScheme.error
            } else if (isHighlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun DottedLineDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 1.5f
        )
    }
}
