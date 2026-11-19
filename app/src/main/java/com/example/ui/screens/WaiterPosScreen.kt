package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.data.EthiopianFoodPresets
import com.example.model.OrderItem
import com.example.ui.components.DottedLineDivider
import com.example.viewmodel.VatCalculatorViewModel
import com.example.viewmodel.VatUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaiterPosScreen(
    uiState: VatUiState,
    viewModel: VatCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency = "ብር"
    val scrollState = rememberScrollState()

    var showEditPriceDialog by remember { mutableStateOf<OrderItem?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. የሬስቶራንቱ እና የአስተናጋጅ ስም መፃፊያ ካርድ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // የሬስቶራንቱ ስም
                OutlinedTextField(
                    value = uiState.restaurantName,
                    onValueChange = { viewModel.setRestaurantName(it) },
                    label = { Text(AppStrings.get(StringKey.RESTAURANT_NAME)) },
                    placeholder = {
                        Text(
                            text = AppStrings.get(StringKey.RESTAURANT_NAME_HINT),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // የአስተናጋጅ ስም
                OutlinedTextField(
                    value = uiState.waiterName,
                    onValueChange = { viewModel.setWaiterName(it) },
                    label = { Text(AppStrings.get(StringKey.WAITER_NAME)) },
                    placeholder = {
                        Text(
                            text = AppStrings.get(StringKey.WAITER_NAME_HINT),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ፈጣን የአስተናጋጅ ስም መምረጫ
                val quickWaiters = listOf("አልማዝ", "ሰለሞን", "መሰረት", "ዳዊት", "ሄለን", "ተስፋዬ")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickWaiters) { name ->
                        val isSelected = uiState.waiterName == name
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setWaiterName(name) },
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // 2. የጠረጴዛ መምረጫ ካርድ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableBar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${AppStrings.get(StringKey.TABLE_NO)}: ${uiState.tableNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // አዲስ ትዕዛዝ / ማጽጃ
                    TextButtonWithIcon(
                        text = AppStrings.get(StringKey.NEW_ORDER),
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.clearWaiterOrder() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ፈጣን የጠረጴዛ ምርጫዎች
                val commonTables = listOf("1", "2", "3", "4", "5", "6", "7", "8", "VIP")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commonTables) { tbl ->
                        val isSelected = uiState.tableNumber == tbl
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTableNumber(tbl) },
                            label = { Text("ጠረጴዛ $tbl") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // 3. የትዕዛዝ ማካተቻ ምርጫ: የምግብ ዝርዝር ወይስ ቀጥታ ሂሳብ
        TabRow(
            selectedTabIndex = uiState.waiterEntryMode,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            indicator = {},
            divider = {}
        ) {
            Tab(
                selected = uiState.waiterEntryMode == 0,
                onClick = { viewModel.setWaiterEntryMode(0) },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.waiterEntryMode == 0) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    ),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "የምግብ ዝርዝር",
                            fontWeight = if (uiState.waiterEntryMode == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            )

            Tab(
                selected = uiState.waiterEntryMode == 1,
                onClick = { viewModel.setWaiterEntryMode(1) },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.waiterEntryMode == 1) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    ),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ቀጥታ ሂሳብ",
                            fontWeight = if (uiState.waiterEntryMode == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            )
        }

        // ሁነታ 0: ፈጣን የምግብ ዝርዝር መራጭ (ያለ ዋጋ በስሙ ላይ!)
        AnimatedVisibility(visible = uiState.waiterEntryMode == 0) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ለመጨመር ምግቡን/መጠጡን ይንኩ:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(onClick = { showAddCustomDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ብጁ ምግብ")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // የምግብ እና የመጠጥ ስሞች ብቻ (ዋጋ በስሙ ላይ የለም!)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EthiopianFoodPresets.list.forEach { preset ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.addWaiterItem(preset) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.nameAm,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // የታዘዙ ምግቦች ዝርዝር
                if (uiState.orderItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "የታዘዙ ምግቦች (${uiState.orderItems.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ዋጋ ለመቀየር ዋጋውን ይንኩ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            uiState.orderItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showEditPriceDialog = item }
                                    ) {
                                        Text(
                                            text = item.nameAm,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${VatCalculatorViewModel.formatMoney(item.unitPrice)} $currency",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "ዋጋ ቀይር",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.updateOrderItemQty(item.id, -1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "ቀንስ", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        IconButton(
                                            onClick = { viewModel.updateOrderItemQty(item.id, 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "ጨምር", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = "${VatCalculatorViewModel.formatMoney(item.totalPrice)} $currency",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(80.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ሁነታ 1: ቀጥታ መነሻ ሂሳብ ማስገቢያ
        AnimatedVisibility(visible = uiState.waiterEntryMode == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "የምግብ እና መጠጥ መነሻ ድምር ያስገቡ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.waiterQuickAmount,
                        onValueChange = { viewModel.onWaiterQuickAmountChanged(it) },
                        label = { Text("መነሻ ዋጋ (ብር)") },
                        placeholder = {
                            Text(
                                text = "0.00",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text(currency, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("waiter_quick_amount_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ፈጣን +100, +200, +500 መጨመሪያ አዝራሮች
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100.0, 200.0, 500.0).forEach { add ->
                            FilledTonalButton(
                                onClick = { viewModel.addWaiterQuickAmount(add) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+${add.toInt()}")
                            }
                        }
                    }
                }
            }
        }

        // 4. የአገልግሎት ክፍያ ማብሪያ/ማጥፊያ (ኦፕሽናል)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.get(StringKey.OPTIONAL_SERVICE_CHARGE),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.includeServiceCharge) "10% የአገልግሎት ክፍያ ተካቷል" else "ሰርቪስ አይካተትም (0%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.includeServiceCharge) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = uiState.includeServiceCharge,
                    onCheckedChange = { viewModel.setIncludeServiceCharge(it) }
                )
            }
        }

        // 5. የኢትዮጵያ ሬስቶራንት ይፋዊ የደረሰኝ ካርድ
        val calc = uiState.waiterCalculation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("waiter_bill_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // ሬስቶራንት እና አስተናጋጅ በደረሰኝ አናት ላይ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = uiState.restaurantName.ifBlank { "የኢትዮጵያ ሬስቶራንት" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "አስተናጋጅ: ${uiState.waiterName} | ጠረጴዛ: ${uiState.tableNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (uiState.includeServiceCharge) "10% ሰርቪስ + 15% ቫት" else "15% ቫት ብቻ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // የሂሳቡ ዝርዝር ክፍፍል
                ReceiptLine(
                    title = "የምግብ/መጠጥ መነሻ ድምር",
                    amount = calc.baseAmount,
                    currency = currency
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ሰርቪስ (ኦፕሽናል)
                if (uiState.includeServiceCharge) {
                    ReceiptLine(
                        title = "10% የአገልግሎት ክፍያ (ሰርቪስ)",
                        amount = calc.serviceAmount,
                        currency = currency
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "የአገልግሎት ክፍያ (ሰርቪስ)",
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

                Spacer(modifier = Modifier.height(8.dp))

                ReceiptLine(
                    title = "15% ተጨማሪ እሴት ታክስ (ቫት)",
                    amount = calc.vatAmount,
                    currency = currency
                )

                Spacer(modifier = Modifier.height(14.dp))
                DottedLineDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // ጠቅላላ ድምር
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ጠቅላላ የሚከፈል:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = VatCalculatorViewModel.formatMoney(calc.totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
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

                Spacer(modifier = Modifier.height(20.dp))

                // ለአስተናጋጇ ፈጣን አዝራሮች
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.shareWaiterReceipt(context) },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ደረሰኝ ላክ")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.copyWaiterReceipt(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ኮፒ")
                    }

                    FilledTonalButton(
                        onClick = { viewModel.saveCurrentWaiterReceipt() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("መዝግብ")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialog: የንጥል ዋጋ ማስተካከያ
    showEditPriceDialog?.let { item ->
        var tempPrice by remember { mutableStateOf(if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toLong().toString() else item.unitPrice.toString()) }
        AlertDialog(
            onDismissRequest = { showEditPriceDialog = null },
            title = { Text("${item.nameAm} ዋጋ ማስተካከያ") },
            text = {
                OutlinedTextField(
                    value = tempPrice,
                    onValueChange = { tempPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("የአንዱ ዋጋ (ብር)") },
                    placeholder = {
                        Text(
                            text = "0.00",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val p = tempPrice.toDoubleOrNull() ?: item.unitPrice
                    viewModel.updateOrderItemPrice(item.id, p)
                    showEditPriceDialog = null
                }) {
                    Text("ቀይር")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPriceDialog = null }) {
                    Text("ተው")
                }
            }
        )
    }

    // Dialog: ብጁ ምግብ/መጠጥ መመዝገቢያ
    if (showAddCustomDialog) {
        var customName by remember { mutableStateOf("") }
        var customPrice by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("አዲስ ምግብ/መጠጥ ጨምር") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("የምግብ ወይም መጠጥ ስም") },
                        placeholder = {
                            Text(
                                text = "ለምሳሌ: ዶሮ ወጥ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customPrice,
                        onValueChange = { customPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("የአንዱ ዋጋ (ብር)") },
                        placeholder = {
                            Text(
                                text = "0.00",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = customPrice.toDoubleOrNull() ?: 0.0
                    if (customName.isNotBlank() && p > 0.0) {
                        viewModel.addCustomOrderItem(customName, p)
                        showAddCustomDialog = false
                    }
                }) {
                    Text("ጨምር")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("ተው")
                }
            }
        )
    }
}

@Composable
private fun ReceiptLine(title: String, amount: Double, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${VatCalculatorViewModel.formatMoney(amount)} $currency",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TextButtonWithIcon(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
