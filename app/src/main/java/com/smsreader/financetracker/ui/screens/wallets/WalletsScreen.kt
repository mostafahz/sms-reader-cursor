package com.smsreader.financetracker.ui.screens.wallets

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smsreader.financetracker.data.local.entities.Wallet
import com.smsreader.financetracker.ui.ViewModelFactory
import com.smsreader.financetracker.ui.theme.*
import com.smsreader.financetracker.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    viewModelFactory: ViewModelFactory
) {
    val viewModel: WalletsViewModel = viewModel(factory = viewModelFactory)
    val wallets by viewModel.wallets.collectAsState()
    val walletSpending by viewModel.walletSpending.collectAsState()
    val walletTransactionCounts by viewModel.walletTransactionCounts.collectAsState()

    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Wallets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (wallets.isEmpty()) {
            EmptyWalletsState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(wallets) { wallet ->
                    ModernWalletCard(
                        wallet = wallet,
                        spending = walletSpending[wallet.walletId] ?: 0.0,
                        transactionCount = walletTransactionCounts[wallet.walletId] ?: 0,
                        displayName = viewModel.getWalletDisplayName(wallet),
                        onRenameClick = {
                            selectedWallet = wallet
                            showRenameDialog = true
                        }
                    )
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showRenameDialog && selectedWallet != null) {
        RenameWalletDialog(
            currentName = viewModel.getWalletDisplayName(selectedWallet!!),
            onDismiss = {
                showRenameDialog = false
                selectedWallet = null
            },
            onConfirm = { newName ->
                viewModel.renameWallet(selectedWallet!!.walletId, newName)
                showRenameDialog = false
                selectedWallet = null
            }
        )
    }
}

@Composable
fun EmptyWalletsState(modifier: Modifier = Modifier) {
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
                text = "No wallets detected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Wallets will appear automatically as transactions are detected from your SMS",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun ModernWalletCard(
    wallet: Wallet,
    spending: Double,
    transactionCount: Int,
    displayName: String,
    onRenameClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with bank name and edit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        wallet.bankName?.let { bank ->
                            Text(
                                text = bank,
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        wallet.cardType?.let { type ->
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRenameClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Card chip simulation
                Box(
                    modifier = Modifier
                        .size(40.dp, 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f))
                )

                // Spending stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = FormatUtils.formatAmount(spending),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = transactionCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RenameWalletDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { 
            Text(
                "Rename Wallet",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Custom Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (newName.isNotBlank()) onConfirm(newName) },
                enabled = newName.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
