package com.smsreader.financetracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smsreader.financetracker.ui.ViewModelFactory
import com.smsreader.financetracker.ui.components.TransactionCard
import com.smsreader.financetracker.ui.theme.*
import com.smsreader.financetracker.ui.util.FormatUtils

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
                title = { 
                    Text(
                        "Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TextButton(
                        onClick = { showTimeRangeMenu = true }
                    ) {
                        Text(
                            text = when (selectedTimeRange) {
                                DashboardViewModel.TimeRange.THIS_MONTH -> "This Month"
                                DashboardViewModel.TimeRange.LAST_3_MONTHS -> "3 Months"
                                DashboardViewModel.TimeRange.ALL_TIME -> "All Time"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select range",
                            modifier = Modifier.size(20.dp)
                        )
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            EmptyDashboard(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hero Card - Total Spending
                item {
                    TotalSpendingCard(
                        amount = totalSpent,
                        transactionCount = transactionCount,
                        topCategory = topCategory
                    )
                }

                // Quick Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickStatCard(
                            title = "Transactions",
                            value = transactionCount.toString(),
                            icon = Icons.Default.Receipt,
                            modifier = Modifier.weight(1f)
                        )
                        topCategory?.let { category ->
                            QuickStatCard(
                                title = "Top Category",
                                value = category.take(8),
                                icon = Icons.Default.Category,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Spending by Category
                if (categorySpending.isNotEmpty()) {
                    item {
                        Text(
                            text = "Spending Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    item {
                        CategorySpendingCard(categorySpending)
                    }
                }

                // Recent Transactions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(transactions.take(10)) { transaction ->
                    TransactionCard(transaction = transaction)
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyDashboard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your financial insights will appear here once you scan your SMS messages",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun TotalSpendingCard(
    amount: Double,
    transactionCount: Int,
    topCategory: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = FormatUtils.formatAmount(amount),
                        style = MaterialTheme.typography.displayMedium,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            text = "$transactionCount",
                            style = MaterialTheme.typography.titleLarge,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    topCategory?.let {
                        Column {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Top Category",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategorySpendingCard(
    categorySpending: List<com.smsreader.financetracker.data.local.dao.CategorySpending>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val total = categorySpending.sumOf { it.total }
            
            categorySpending.take(5).forEach { spending ->
                CategorySpendingRow(
                    category = spending.category,
                    amount = spending.total,
                    percentage = if (total > 0) (spending.total / total * 100).toFloat() else 0f
                )
            }
        }
    }
}

@Composable
fun CategorySpendingRow(
    category: String,
    amount: Double,
    percentage: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "%.1f%% of spending".format(percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = FormatUtils.formatAmount(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
