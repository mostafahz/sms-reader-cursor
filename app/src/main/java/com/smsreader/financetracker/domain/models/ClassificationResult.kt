package com.smsreader.financetracker.domain.models

import com.smsreader.financetracker.data.local.entities.CardType
import com.smsreader.financetracker.data.local.entities.TransactionType

data class ClassificationResult(
    val amount: Double,
    val merchant: String,
    val category: String,
    val walletId: String,
    val cardType: CardType,
    val transactionType: TransactionType,
    val detectedWalletName: String,
    val bankName: String?
)

