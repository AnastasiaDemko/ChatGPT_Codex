package com.example.financeapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.financeapp.model.Account
import com.example.financeapp.model.Category
import com.example.financeapp.model.FinanceOperation
import com.example.financeapp.model.HistoryFilter
import com.example.financeapp.model.OperationType
import com.example.financeapp.model.availableColors
import com.example.financeapp.model.availableIcons
import java.time.LocalDate

class FinanceStore {
    val accounts = mutableStateListOf<Account>()
    val categories = mutableStateListOf<Category>()
    val operations = mutableStateListOf<FinanceOperation>()

    var selectedFilter by mutableStateOf(HistoryFilter.MONTH)

    init {
        seedData()
    }

    private fun seedData() {
        val cash = Account(name = "Наличные", icon = availableIcons[0], color = availableColors[0], balance = 350.0)
        val card = Account(name = "Карта", icon = availableIcons[2], color = availableColors[1], balance = 1200.0)
        accounts.addAll(listOf(cash, card))

        categories.addAll(
            listOf(
                Category(name = "Питание", icon = availableIcons[3], color = availableColors[2], operationType = OperationType.EXPENSE),
                Category(name = "Транспорт", icon = availableIcons[4], color = availableColors[3], operationType = OperationType.EXPENSE),
                Category(name = "Аренда", icon = availableIcons[5], color = availableColors[4], operationType = OperationType.EXPENSE),
                Category(name = "Зарплата", icon = availableIcons[1], color = availableColors[0], operationType = OperationType.INCOME)
            )
        )
    }

    fun addAccount(account: Account) = accounts.add(account)

    fun updateAccount(updated: Account) {
        val index = accounts.indexOfFirst { it.id == updated.id }
        if (index >= 0) accounts[index] = updated
    }

    fun deleteAccount(accountId: String) {
        accounts.removeAll { it.id == accountId }
    }

    fun addCategory(category: Category) = categories.add(category)

    fun updateCategory(updated: Category) {
        val index = categories.indexOfFirst { it.id == updated.id }
        if (index >= 0) categories[index] = updated
    }

    fun deleteCategory(categoryId: String) {
        categories.removeAll { it.id == categoryId }
    }

    fun addOperation(operation: FinanceOperation) {
        when (operation.type) {
            OperationType.INCOME -> updateBalance(operation.accountId, operation.amount)
            OperationType.EXPENSE -> updateBalance(operation.accountId, -operation.amount)
            OperationType.TRANSFER -> {
                operation.sourceAccountId?.let { updateBalance(it, -operation.amount) }
                operation.targetAccountId?.let { updateBalance(it, operation.amount) }
            }
        }
        operations.add(0, operation)
    }

    private fun updateBalance(accountId: String, diff: Double) {
        val index = accounts.indexOfFirst { it.id == accountId }
        if (index >= 0) {
            val account = accounts[index]
            accounts[index] = account.copy(balance = account.balance + diff)
        }
    }

    fun filteredOperations(): List<FinanceOperation> {
        val now = LocalDate.now()
        return operations.filter {
            when (selectedFilter) {
                HistoryFilter.DAY -> it.date == now
                HistoryFilter.WEEK -> !it.date.isBefore(now.minusDays(6))
                HistoryFilter.MONTH -> !it.date.isBefore(now.minusDays(29))
                HistoryFilter.ALL -> true
            }
        }
    }
}
