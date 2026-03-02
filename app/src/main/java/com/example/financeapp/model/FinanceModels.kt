package com.example.financeapp.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.util.UUID

enum class OperationType { INCOME, EXPENSE, TRANSFER }

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val balance: Double
)

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val operationType: OperationType
)

data class FinanceOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val amount: Double,
    val accountId: String,
    val categoryId: String?,
    val date: LocalDate,
    val sourceAccountId: String? = null,
    val targetAccountId: String? = null
)

enum class HistoryFilter { DAY, WEEK, MONTH, ALL }

val availableIcons: List<ImageVector> = listOf(
    Icons.Default.AccountBalanceWallet,
    Icons.Default.AttachMoney,
    Icons.Default.Payments,
    Icons.Default.Restaurant,
    Icons.Default.DirectionsBus,
    Icons.Default.Home
)

val availableColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF03A9F4),
    Color(0xFFFF9800),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF607D8B)
)
