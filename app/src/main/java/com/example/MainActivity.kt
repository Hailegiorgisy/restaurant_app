package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppStrings
import com.example.data.AppStrings.StringKey
import com.example.model.Language
import com.example.ui.screens.HistoryDialog
import com.example.ui.screens.OwnerTaxScreen
import com.example.ui.screens.TaxInfoDialog
import com.example.ui.screens.WaiterPosScreen
import com.example.ui.screens.WaitersDailyCollectionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VatCalculatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VatCalculatorViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                var showHistoryDialog by remember { mutableStateOf(false) }
                var showTaxInfoDialog by remember { mutableStateOf(false) }

                LaunchedEffect(uiState.userMessage) {
                    uiState.userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUserMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = AppStrings.get(StringKey.APP_NAME),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            actions = {
                                // የቀደሙ ደረሰኞች ታሪክ
                                IconButton(
                                    onClick = { showHistoryDialog = true },
                                    modifier = Modifier.testTag("history_button")
                                ) {
                                    if (uiState.history.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge { Text("${uiState.history.size}") }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = AppStrings.get(StringKey.HISTORY_TITLE)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = AppStrings.get(StringKey.HISTORY_TITLE)
                                        )
                                    }
                                }

                                // የታክስ መመሪያ መረጃ
                                IconButton(
                                    onClick = { showTaxInfoDialog = true },
                                    modifier = Modifier.testTag("info_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "የታክስ መመሪያ"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        // 3-tab navigation bar (pure Amharic)
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.setTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = AppStrings.get(StringKey.TAB_WAITER)
                                    )
                                },
                                label = {
                                    Text(
                                        text = AppStrings.get(StringKey.TAB_WAITER),
                                        fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("tab_waiter")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.setTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = AppStrings.get(StringKey.TAB_WAITERS_SALES)
                                    )
                                },
                                label = {
                                    Text(
                                        text = AppStrings.get(StringKey.TAB_WAITERS_SALES),
                                        fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("tab_waiters_sales")
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 2,
                                onClick = { viewModel.setTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = AppStrings.get(StringKey.TAB_OWNER)
                                    )
                                },
                                label = {
                                    Text(
                                        text = AppStrings.get(StringKey.TAB_OWNER),
                                        fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("tab_owner")
                            )
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = uiState.selectedTab,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { tab ->
                        when (tab) {
                            0 -> WaiterPosScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            1 -> WaitersDailyCollectionScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            2 -> OwnerTaxScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onShowTaxInfo = { showTaxInfoDialog = true }
                            )
                        }
                    }

                    if (showHistoryDialog) {
                        HistoryDialog(
                            history = uiState.history,
                            language = Language.AMHARIC,
                            onDismiss = { showHistoryDialog = false },
                            onDelete = { viewModel.deleteHistoryItem(it) },
                            onClearAll = { viewModel.clearAllHistory() },
                            onCopy = { viewModel.copyReceiptToClipboard(context, it) }
                        )
                    }

                    if (showTaxInfoDialog) {
                        TaxInfoDialog(
                            language = Language.AMHARIC,
                            onDismiss = { showTaxInfoDialog = false }
                        )
                    }
                }
            }
        }
    }
}
