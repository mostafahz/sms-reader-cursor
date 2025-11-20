package com.smsreader.financetracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smsreader.financetracker.ui.ViewModelFactory
import com.smsreader.financetracker.ui.components.TransactionCard
import com.smsreader.financetracker.ui.util.FormatUtils
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModelFactory: ViewModelFactory
) {
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val transactions by viewModel.transactions.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val transactionCount by viewModel.transactionCount.collectAsState()
    val categorySpending by viewModel.categorySpending.collectAsState()
    val topCategory by viewModel.topCategory.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()

    var showTimeRangeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { showTimeRangeMenu = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Time Range")
                    }

                    DropdownMenu(
                        expanded = showTimeRangeMenu,
                        onDismissRequest = { showTimeRangeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("This Month") },
                            onClick = {
                                viewModel.setTimeRange(DashboardViewModel.TimeRange.THIS_MONTH)
                                showTimeRangeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Last 3 Months") },
                            onClick = {
                                viewModel.setTimeRange(DashboardViewModel.TimeRange.LAST_3_MONTHS)
                                showTimeRangeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All Time") },
                            onClick = {
                                viewModel.setTimeRange(DashboardViewModel.TimeRange.ALL_TIME)
                                showTimeRangeMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Scan your SMS to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Total Spent",
                            value = FormatUtils.formatCompactNumber(totalSpent),
                            icon = Icons.Default.AccountBalanceWallet,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Transactions",
                            value = transactionCount.toString(),
                            icon = Icons.Default.Receipt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    topCategory?.let { category ->
                        SummaryCard(
                            title = "Top Category",
                            value = category,
                            icon = Icons.Default.Category,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Spending by Category
                if (categorySpending.isNotEmpty()) {
                    item {
                        Text(
                            text = "Spending by Category",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        CategorySpendingList(categorySpending)
                    }
                }

                // Recent Transactions
                item {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(transactions.take(10)) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun CategorySpendingList(
    categorySpending: List<com.smsreader.financetracker.data.local.dao.CategorySpending>
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val total = categorySpending.sumOf { it.total }
            
            categorySpending.forEach { spending ->
                CategorySpendingItem(
                    category = spending.category,
                    amount = spending.total,
                    percentage = if (total > 0) (spending.total / total * 100).toFloat() else 0f
                )
            }
        }
    }
}

@Composable
fun CategorySpendingItem(
    category: String,
    amount: Double,
    percentage: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = FormatUtils.formatAmount(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = "%.1f%%".format(percentage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

