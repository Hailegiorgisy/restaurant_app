package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.model.SavedReceipt
import com.example.model.WaiterShiftReport
import com.example.ui.components.DottedLineDivider
import com.example.viewmodel.VatCalculatorViewModel
import com.example.viewmodel.VatUiState

@Composable
fun WaitersDailyCollectionScreen(
    uiState: VatUiState,
    viewModel: VatCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency = "ብር"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("waiters_collection_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // የገቢ መረከቢያ ርዕስ እና የሬስቶራንት መረጃ
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.get(StringKey.WAITERS_DAILY_COLLECTION),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = uiState.restaurantName.ifBlank { "የኢትዮጵያ ሬስቶራንት" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (uiState.history.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearAllHistory() }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "ሁሉንም አጽዳ",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 የጥሬ ገንዘብ ማጠቃለያ ካርዶች
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ጠቅላላ የሚሰበሰብ
                        MetricPillCard(
                            modifier = Modifier.weight(1f),
                            title = "ጠቅላላ የሚሰበሰብ",
                            amount = uiState.shiftGrossSales,
                            currency = currency,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // የተረከበው
                        MetricPillCard(
                            modifier = Modifier.weight(1f),
                            title = "የተረከበው ሂሳብ",
                            amount = uiState.totalSettledCash,
                            currency = currency,
                            color = Color(0xFF2E7D32)
                        )

                        // ያልተረከበው / በእጅ ያለ
                        MetricPillCard(
                            modifier = Modifier.weight(1f),
                            title = "ያልተረከበ (በእጅ)",
                            amount = uiState.totalPendingCash,
                            currency = currency,
                            color = if (uiState.totalPendingCash > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    DottedLineDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // የቫት እና የአገልግሎት ክፍያ ማስታወሻ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ለገቢዎች የሚላክ 15% ቫት: ${VatCalculatorViewModel.formatMoney(uiState.shiftVatToRemit)} $currency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "የሰርቪስ ገንዳ: ${VatCalculatorViewModel.formatMoney(uiState.shiftStaffServicePool)} $currency",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // የአስተናጋጆች ዝርዝር ርዕስ
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "የእያንዳንዱ አስተናጋጅ የቀን ሂሳብ (${uiState.waiterShiftReports.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "የኢትዮጵያ አሰራር",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ምንም የተመዘገበ ሽያጭ ከሌለ
        if (uiState.waiterShiftReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "እስካሁን የተመዘገበ የአስተናጋጅ ሽያጭ የለም",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "በ'የአስተናጋጅ ሂሳብ' ገፅ ላይ ደረሰኝ ሲያስቀምጡ እዚህ ይዘረዘራል",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            // የእያንዳንዱ አስተናጋጅ ካርድ
            items(uiState.waiterShiftReports, key = { it.waiterName }) { report ->
                val waiterReceipts = uiState.history.filter { it.waiterName.equals(report.waiterName, ignoreCase = true) }
                WaiterCollectionCard(
                    report = report,
                    receipts = waiterReceipts,
                    currency = currency,
                    onSettle = { viewModel.settleWaiterSales(report.waiterName) },
                    onShareReport = { viewModel.shareWaiterShiftReport(context, report.waiterName) },
                    onToggleReceiptSettled = { viewModel.toggleReceiptSettled(it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WaiterCollectionCard(
    report: WaiterShiftReport,
    receipts: List<SavedReceipt>,
    currency: String,
    onSettle: () -> Unit,
    onShareReport: () -> Unit,
    onToggleReceiptSettled: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isFullySettled = report.pendingCash <= 0.001

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Waiter Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFullySettled) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isFullySettled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = report.waiterName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${report.totalOrders} ጠረጴዛዎች ተስተናግደዋል",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFullySettled) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFullySettled) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (isFullySettled) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFullySettled) "ተረክቧል ✓" else "ያልተረከበ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullySettled) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Highlight: Cash to collect from this waiter
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.get(StringKey.CASH_TO_COLLECT),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = VatCalculatorViewModel.formatMoney(report.totalCashToCollect),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!isFullySettled) {
                        Button(
                            onClick = onSettle,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PriceCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ተረከብኩ")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("የምግብ መነሻ: ${VatCalculatorViewModel.formatMoney(report.totalBaseRevenue)} $currency", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("15% ቫት: ${VatCalculatorViewModel.formatMoney(report.totalVat)} $currency", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ሰርቪስ: ${VatCalculatorViewModel.formatMoney(report.totalServiceCharge)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Share Handover Slip & Expand Tables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareReport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ሪፖርት ላክ")
                }

                FilledTonalButton(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ዝርዝር (${receipts.size})")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expandable List of Tables
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DottedLineDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    receipts.forEach { receipt ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ጠረጴዛ ${receipt.tableNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "መነሻ: ${VatCalculatorViewModel.formatMoney(receipt.baseAmount)} | ቫት: ${VatCalculatorViewModel.formatMoney(receipt.vatAmount)} $currency",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${VatCalculatorViewModel.formatMoney(receipt.totalAmount)} $currency",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                        color = if (receipt.isSettled) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                    ) {
                                        Text(
                                            text = if (receipt.isSettled) "✓" else "...",
                                            fontWeight = FontWeight.Bold,
                                            color = if (receipt.isSettled) Color(0xFF2E7D32) else Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPillCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    currency: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = VatCalculatorViewModel.formatMoney(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
            Text(
                text = currency,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
