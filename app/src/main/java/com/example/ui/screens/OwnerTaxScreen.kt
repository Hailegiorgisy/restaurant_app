package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.ui.components.DottedLineDivider
import com.example.viewmodel.VatCalculatorViewModel
import com.example.viewmodel.VatUiState

@Composable
fun OwnerTaxScreen(
    uiState: VatUiState,
    viewModel: VatCalculatorViewModel,
    onShowTaxInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = "ብር"
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // የቀን/ፈረቃ ሽያጭ እና ታክስ ማጠቃለያ ካርድ (ለባለቤቶች እና ማኔጀሮች)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppStrings.get(StringKey.SHIFT_SUMMARY),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${AppStrings.get(StringKey.ORDERS_COUNT)}: ${uiState.shiftTotalOrders}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(onClick = onShowTaxInfo) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "የታክስ መረጃ",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ጠቅላላ የሽያጭ ገቢ
                Text(
                    text = AppStrings.get(StringKey.TOTAL_SALES),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = VatCalculatorViewModel.formatMoney(uiState.shiftGrossSales),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                DottedLineDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                // የሽያጭ ዝርዝር ለታክስ ሪፖርት
                SummaryMetricRow(
                    label = AppStrings.get(StringKey.TOTAL_VAT_COLLECTED),
                    amount = uiState.shiftVatToRemit,
                    currency = currency,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                SummaryMetricRow(
                    label = AppStrings.get(StringKey.TOTAL_SERVICE_POOL),
                    amount = uiState.shiftStaffServicePool,
                    currency = currency,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                SummaryMetricRow(
                    label = AppStrings.get(StringKey.TOTAL_BASE_REVENUE),
                    amount = uiState.shiftNetBaseRevenue,
                    currency = currency,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // የሜኑ ዋጋ መወሰኛ እና መነሻ ማወቂያ ካርድ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "የምግብ ዋጋ እና ታክስ ማስያ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "የመነሻ ዋጋ ወይም ለደንበኛ የሚቀርበውን የሜኑ ዋጋ ለመወሰን ይጠቀሙ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // በሁነታ 0 (የሜኑ ዋጋ መወሰኛ) እና ሁነታ 1 (መነሻ ማወቂያ) መካከል መቀያየሪያ
                TabRow(
                    selectedTabIndex = uiState.ownerMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(14.dp)),
                    indicator = {},
                    divider = {}
                ) {
                    Tab(
                        selected = uiState.ownerMode == 0,
                        onClick = { viewModel.setOwnerMode(0) },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.ownerMode == 0) MaterialTheme.colorScheme.surface
                                else androidx.compose.ui.graphics.Color.Transparent
                            ),
                        text = {
                            Text(
                                text = "መነሻ ➔ የሜኑ ዋጋ",
                                fontWeight = if (uiState.ownerMode == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )

                    Tab(
                        selected = uiState.ownerMode == 1,
                        onClick = { viewModel.setOwnerMode(1) },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.ownerMode == 1) MaterialTheme.colorScheme.surface
                                else androidx.compose.ui.graphics.Color.Transparent
                            ),
                        text = {
                            Text(
                                text = "የሜኑ ዋጋ ➔ መነሻ",
                                fontWeight = if (uiState.ownerMode == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ሁነታ 0: መነሻ ➔ የሜኑ ዋጋ
                if (uiState.ownerMode == 0) {
                    OutlinedTextField(
                        value = uiState.ownerBaseInput,
                        onValueChange = { viewModel.onOwnerBaseInputChanged(it) },
                        label = { Text(AppStrings.get(StringKey.KITCHEN_BASE_PRICE)) },
                        placeholder = {
                            Text(
                                text = AppStrings.get(StringKey.BASE_PRICE_HINT),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                            )
                        },
                        trailingIcon = { Text(currency, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val calc = uiState.ownerBaseCalculation
                    PricingResultBox(
                        title = "ለደንበኛ የሚቀርበው የሜኑ ዋጋ (ከ10% ሰርቪስ እና 15% ቫት ጋር)",
                        highlightAmount = calc.totalAmount,
                        baseAmount = calc.baseAmount,
                        serviceAmount = calc.serviceAmount,
                        vatAmount = calc.vatAmount,
                        currency = currency
                    )
                } else {
                    // ሁነታ 1: የሜኑ ዋጋ ➔ መነሻ እና ታክስ
                    OutlinedTextField(
                        value = uiState.ownerTotalInput,
                        onValueChange = { viewModel.onOwnerTotalInputChanged(it) },
                        label = { Text(AppStrings.get(StringKey.MENU_PRICE_TO_CUSTOMER)) },
                        placeholder = {
                            Text(
                                text = AppStrings.get(StringKey.TOTAL_PRICE_HINT),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                            )
                        },
                        trailingIcon = { Text(currency, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val calc = uiState.ownerTotalCalculation
                    PricingResultBox(
                        title = "የተጣራ የምግብ/መጠጥ መነሻ ገቢ",
                        highlightAmount = calc.baseAmount,
                        baseAmount = calc.baseAmount,
                        serviceAmount = calc.serviceAmount,
                        vatAmount = calc.vatAmount,
                        currency = currency
                    )
                }
            }
        }

        // የግብር እና የአገልግሎት ክፍያ ቅንብሮች
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "የግብር እና የአገልግሎት ቅንብሮች",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // የአገልግሎት ምጣኔ መምረጫ
                Text(
                    text = "${AppStrings.get(StringKey.SERVICE_RATE)}: ${(uiState.serviceRate * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.0, 0.05, 0.10, 0.15).forEach { rate ->
                        val isSelected = kotlin.math.abs(uiState.serviceRate - rate) < 0.001
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setServiceRate(rate) },
                            label = { Text(if (rate == 0.0) "0%" else "${(rate * 100).toInt()}%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ቫት በአገልግሎት ክፍያ ላይ ይሰላል የሚል ማብሪያ/ማጥፊያ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.get(StringKey.VAT_ON_SERVICE_SWITCH),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "እንደ ገቢዎች ሚኒስቴር ይፋዊ ደንብ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = uiState.vatOnService,
                        onCheckedChange = { viewModel.setVatOnService(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryMetricRow(label: String, amount: Double, currency: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.85f)
        )
        Text(
            text = "${VatCalculatorViewModel.formatMoney(amount)} $currency",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun PricingResultBox(
    title: String,
    highlightAmount: Double,
    baseAmount: Double,
    serviceAmount: Double,
    vatAmount: Double,
    currency: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = VatCalculatorViewModel.formatMoney(highlightAmount),
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

            Spacer(modifier = Modifier.height(10.dp))
            DottedLineDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("መነሻ ዋጋ:", style = MaterialTheme.typography.bodySmall)
                Text("${VatCalculatorViewModel.formatMoney(baseAmount)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10% የአገልግሎት ክፍያ:", style = MaterialTheme.typography.bodySmall)
                Text("${VatCalculatorViewModel.formatMoney(serviceAmount)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("15% ተጨማሪ እሴት ታክስ:", style = MaterialTheme.typography.bodySmall)
                Text("${VatCalculatorViewModel.formatMoney(vatAmount)} $currency", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
