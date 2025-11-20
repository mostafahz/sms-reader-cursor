package com.smsreader.financetracker.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.opencsv.CSVWriter
import com.smsreader.financetracker.data.local.entities.Transaction
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    
    fun exportTransactionsToCsv(
        context: Context,
        transactions: List<Transaction>
    ): Result<File> {
        return try {
            // Create file in app's external files directory
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "transactions_$timestamp.csv"
            
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            // Create CSV writer
            val csvWriter = CSVWriter(FileWriter(file))
            
            // Write header
            csvWriter.writeNext(
                arrayOf(
                    "Date",
                    "Time",
                    "Amount (INR)",
                    "Merchant",
                    "Category",
                    "Wallet",
                    "Type",
                    "Sender ID"
                )
            )
            
            // Write transactions
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            
            for (transaction in transactions) {
                val date = Date(transaction.timestamp)
                csvWriter.writeNext(
                    arrayOf(
                        dateFormat.format(date),
                        timeFormat.format(date),
                        transaction.amount.toString(),
                        transaction.merchant,
                        transaction.category,
                        transaction.walletId,
                        transaction.transactionType.toString(),
                        transaction.senderId
                    )
                )
            }
            
            csvWriter.close()
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Transactions CSV"))
    }
}

