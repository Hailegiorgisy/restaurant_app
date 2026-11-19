package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.model.DailySaleSummary
import com.example.util.EthiopianDateUtil
import com.example.viewmodel.VatCalculatorViewModel
import com.example.viewmodel.VatUiState

@Composable
fun WeeklyDailySalesScreen(
    uiState: VatUiState,
    viewModel: VatCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEthioTimeDialog by remember { mutableStateOf(false) }
    var showConfirmSaveDayDialog by remember { mutableStateOf(false) }
    var selectedDayDetail by remember { mutableStateOf<DailySaleSummary?>(null) }

    val currency = "ብር"
    val ethDateNow = remember { EthiopianDateUtil.toEthiopianDate() }
    val ethTimeNow = remember { EthiopianDateUtil.toEthiopianTime() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("weekly_daily_sales_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ethiopian Calendar & Time Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEthioTimeDialog = true }
                    .testTag("ethiopian_time_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "📅 ${ethDateNow.fullDateWithDay}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "⏰ ${ethTimeNow.formattedTime} (የኢትዮጵያ ሰዓት)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEthioTimeDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "ስለ ሰዓቱ መረጃ",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Today's Live Sales & Save Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_shift_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "የዛሬ የቀን ሽያጭ (ዛሬ ${ethDateNow.dayOfWeekName})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ethDateNow.formattedDate} • ${ethTimeNow.formattedTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = AppStrings.get(StringKey.DAY_ACTIVE),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Stat grid for Today
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "ጠቅላላ የዛሬ ሽያጭ",
                            amount = uiState.shiftGrossSales,
                            currency = currency,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "15% ቫት (ለመንግስት)",
                            amount = uiState.shiftVatToRemit,
                            currency = currency,
                            color = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "የሰርቪስ ገንዳ",
                            amount = uiState.shiftStaffServicePool,
                            currency = currency,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "የተረከበ ጥሬ ገንዘብ",
                            amount = uiState.totalSettledCash,
                            currency = currency,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action: Save Daily Sale (መዝግብ እና አስቀምጥ)
                    Button(
                        onClick = { showConfirmSaveDayDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_daily_sale_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get(StringKey.SAVE_DAILY_SALE),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Weekly Overview (7 ቀናት) Stats & Share Report
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get(StringKey.WEEKLY_SALES_TITLE),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "7 ቀናት",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Metric Rows
                    WeeklyMetricRow(
                        label = AppStrings.get(StringKey.WEEKLY_TOTAL_GROSS),
                        amount = uiState.weeklyGrossSales,
                        currency = currency,
                        icon = Icons.Default.Receipt,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WeeklyMetricRow(
                        label = AppStrings.get(StringKey.WEEKLY_TOTAL_VAT),
                        amount = uiState.weeklyVatTotal,
                        currency = currency,
                        icon = Icons.Default.AccountBalance,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WeeklyMetricRow(
                        label = AppStrings.get(StringKey.WEEKLY_TOTAL_SERVICE),
                        amount = uiState.weeklyServiceTotal,
                        currency = currency,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Share Weekly Report Button
                    OutlinedButton(
                        onClick = { viewModel.shareWeeklyReport(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("share_weekly_report_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.get(StringKey.SHARE_WEEKLY_REPORT),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Section Header for Daily Records
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "የእያንዳንዱ ቀን ዝርዝር መዝገብ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${uiState.weeklyDailySales.size} ቀናት ተመዝግበዋል",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. Daily Records List (At least a week)
        if (uiState.weeklyDailySales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "እስካሁን የተመዘገበ የቀን ሽያጭ የለም",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "የዛሬውን ሽያጭ ለማስቀመጥ 'የቀን ሽያጭ መዝግብ' የሚለውን ይጫኑ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            items(uiState.weeklyDailySales, key = { it.dateKey }) { daySale ->
                DailySaleCard(
                    daySale = daySale,
                    currency = currency,
                    onShareDay = { viewModel.shareDailySaleReport(context, daySale.dateKey) },
                    onSelect = { selectedDayDetail = daySale }
                )
            }
        }
    }

    // Dialog 1: Ethiopian Calendar and Time Information Dialog
    if (showEthioTimeDialog) {
        AlertDialog(
            onDismissRequest = { showEthioTimeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.get(StringKey.ETHIOPIAN_CALENDAR_TITLE),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "☀️ የኢትዮጵያ ሰዓት አቆጣጠር:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• በኢትዮጵያ ጠዋት የሚጀምረው በ12:00 ሰዓት (6:00 AM) ላይ ነው።\n" +
                                "• ቀኑ በ12:00 ሰዓት (6:00 PM) ይጠናቀቃል።\n" +
                                "• ለምሳሌ፦ 8:00 AM = ጠዋት 2:00 ሰዓት፤ 2:00 PM = ከሰዓት 8:00 ሰዓት፤ 8:00 PM = ማታ 2:00 ሰዓት ነው።",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "📅 የኢትዮጵያ ዘመን አቆጣጠር:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• 12 ወራት እያንዳንዳቸው እኩል 30 ቀናት አሏቸው።\n" +
                                "• 13ኛዋ ወር ጳጉሜን 5 ቀናት (በዘመነ ዮሐንስ/ሉቃስ 6 ቀናት) አሏት።\n" +
                                "• አዲሱ ዓመት መስከረም 1 በፈረንጆች ሴፕቴምበር 11 ወይም 12 ይጀምራል።",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showEthioTimeDialog = false }) {
                    Text(AppStrings.get(StringKey.UNDERSTOOD))
                }
            }
        )
    }

    // Dialog 2: Confirm Save Daily Sale Dialog
    if (showConfirmSaveDayDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSaveDayDialog = false },
            title = {
                Text(
                    text = "የቀን ሽያጭ መዝግብ እና አስቀምጥ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "የዛሬውን (${ethDateNow.formattedDate}) ጠቅላላ የሽያጭ፣ የቫት (15%) እና የሰርቪስ መዝገብ በቋሚነት ወደ ዳታቤዝ ማስቀመጥ ይፈልጋሉ?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• ጠቅላላ ሽያጭ: ${VatCalculatorViewModel.formatMoney(uiState.shiftGrossSales)} $currency\n" +
                                "• 15% ቫት: ${VatCalculatorViewModel.formatMoney(uiState.shiftVatToRemit)} $currency\n" +
                                "• ሰርቪስ: ${VatCalculatorViewModel.formatMoney(uiState.shiftStaffServicePool)} $currency\n" +
                                "• ደረሰኞች ብዛት: ${uiState.shiftTotalOrders}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveDailySaleForCurrentDay()
                        showConfirmSaveDayDialog = false
                    }
                ) {
                    Text("አዎ፣ መዝግብ እና አስቀምጥ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSaveDayDialog = false }) {
                    Text("ይቅር")
                }
            }
        )
    }

    // Dialog 3: Day Detail Modal
    if (selectedDayDetail != null) {
        val detail = selectedDayDetail!!
        AlertDialog(
            onDismissRequest = { selectedDayDetail = null },
            title = {
                Text(
                    text = "የ${detail.dayOfWeek} (${detail.ethiopianDateText}) ሙሉ ሪፖርት",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "ጠቅላላ ሽያጭ:", value = "${VatCalculatorViewModel.formatMoney(detail.grossSales)} $currency")
                    DetailRow(label = "የመነሻ ገቢ:", value = "${VatCalculatorViewModel.formatMoney(detail.baseRevenue)} $currency")
                    DetailRow(label = "15% ቫት (ለመንግስት):", value = "${VatCalculatorViewModel.formatMoney(detail.vatToRemit)} $currency")
                    DetailRow(label = "የሰርቪስ ገንዳ:", value = "${VatCalculatorViewModel.formatMoney(detail.servicePool)} $currency")
                    DetailRow(label = "የተረከበ ጥሬ ገንዘብ:", value = "${VatCalculatorViewModel.formatMoney(detail.settledCash)} $currency")
                    DetailRow(label = "ያልተረከበ ጥሬ ገንዘብ:", value = "${VatCalculatorViewModel.formatMoney(detail.pendingCash)} $currency")
                    DetailRow(label = "የትዕዛዞች ብዛት:", value = "${detail.totalOrders} ደረሰኞች")
                    DetailRow(label = "የመዝገብ ሁኔታ:", value = if (detail.isClosed) "የተዘጋ / የተጠናቀቀ" else "ክፍት ፈረቃ")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.shareDailySaleReport(context, detail.dateKey)
                        selectedDayDetail = null
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ሪፖርት ላክ")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDayDetail = null }) {
                    Text(AppStrings.get(StringKey.CLOSE))
                }
            }
        )
    }
}

@Composable
private fun StatTile(
    title: String,
    amount: Double,
    currency: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${VatCalculatorViewModel.formatMoney(amount)} $currency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun WeeklyMetricRow(
    label: String,
    amount: Double,
    currency: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "${VatCalculatorViewModel.formatMoney(amount)} $currency",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun DailySaleCard(
    daySale: DailySaleSummary,
    currency: String,
    onShareDay: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("day_sale_card_${daySale.dateKey}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${daySale.dayOfWeek}፣ ${daySale.ethiopianDateText}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${daySale.totalOrders} ደረሰኞች ተስተናግደዋል",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (daySale.isClosed) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (daySale.isClosed) "የተዘጋ ✓" else "ክፍት ፈረቃ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (daySale.isClosed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row with Key Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ጠቅላላ ሽያጭ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${VatCalculatorViewModel.formatMoney(daySale.grossSales)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(text = "15% ቫት", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${VatCalculatorViewModel.formatMoney(daySale.vatToRemit)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
                Column {
                    Text(text = "ሰርቪስ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${VatCalculatorViewModel.formatMoney(daySale.servicePool)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onShareDay,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ሪፖርት ላክ", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
