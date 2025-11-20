package com.smsreader.financetracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smsreader.financetracker.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModelFactory: ViewModelFactory
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val senderIds by viewModel.senderIds.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()

    var selectedSenderId by remember { mutableStateOf<String?>(null) }
    var messageCount by remember { mutableStateOf(10) }
    var showSenderDropdown by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSenderIds(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SMS Scanner Section
            item {
                Text(
                    text = "SMS Scanner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sender ID Selector
                        ExposedDropdownMenuBox(
                            expanded = showSenderDropdown,
                            onExpandedChange = { showSenderDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedSenderId ?: "All Senders",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Sender ID") },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showSenderDropdown,
                                onDismissRequest = { showSenderDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Senders") },
                                    onClick = {
                                        selectedSenderId = null
                                        showSenderDropdown = false
                                    }
                                )
                                senderIds.take(20).forEach { senderId ->
                                    DropdownMenuItem(
                                        text = { Text(senderId) },
                                        onClick = {
                                            selectedSenderId = senderId
                                            showSenderDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Message Count Slider
                        Column {
                            Text(
                                text = "Number of Messages: $messageCount",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = messageCount.toFloat(),
                                onValueChange = { messageCount = it.toInt() },
                                valueRange = 1f..100f,
                                steps = 98
                            )
                        }

                        // Scan Button
                        Button(
                            onClick = {
                                viewModel.scanMessages(context, selectedSenderId, messageCount)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isScanning
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning...")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Now")
                            }
                        }

                        // Scan Result
                        scanResult?.let { result ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Scan Complete",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Scanned ${result.scannedCount} messages, found ${result.foundTransactions} transactions",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Data Management Section
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export CSV
                        Button(
                            onClick = {
                                viewModel.exportToCsv(context)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = exportStatus !is SettingsViewModel.ExportStatus.Exporting
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export as CSV")
                        }

                        // Export Status
                        when (val status = exportStatus) {
                            is SettingsViewModel.ExportStatus.Exporting -> {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Text("Exporting...", style = MaterialTheme.typography.bodySmall)
                            }
                            is SettingsViewModel.ExportStatus.Success -> {
                                Text(
                                    "Exported successfully!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is SettingsViewModel.ExportStatus.Error -> {
                                Text(
                                    "Error: ${status.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {}
                        }

                        Divider()

                        // Clear Data
                        OutlinedButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Data")
                        }
                    }
                }
            }

            // App Info Section
            item {
                Text(
                    text = "App Info",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow("Version", "1.0.0")
                        InfoRow("Developer", "SMS Finance Tracker")
                    }
                }
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("Are you sure you want to delete all transactions and wallets? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

