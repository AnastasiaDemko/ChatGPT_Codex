package com.example.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.financeapp.data.FinanceStore
import com.example.financeapp.model.Account
import com.example.financeapp.model.Category
import com.example.financeapp.model.FinanceOperation
import com.example.financeapp.model.HistoryFilter
import com.example.financeapp.model.OperationType
import com.example.financeapp.model.availableColors
import com.example.financeapp.model.availableIcons
import com.example.financeapp.ui.theme.FinanceTrackerTheme
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTrackerTheme {
                FinanceApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(store: FinanceStore = remember { FinanceStore() }) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showOperationDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Учёт финансов • BYN") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTab) {
                    0 -> showOperationDialog = true
                    1 -> showTransferDialog = true
                    2 -> showAccountDialog = true
                    else -> showCategoryDialog = true
                }
            }) { Icon(Icons.Default.Add, contentDescription = null) }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Операции", "История", "Счета", "Категории").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> OperationsTab(store)
                1 -> HistoryTab(store)
                2 -> AccountsTab(store)
                3 -> CategoriesTab(store)
            }
        }
    }

    if (showAccountDialog) {
        EditAccountDialog(onDismiss = { showAccountDialog = false }, onSave = {
            store.addAccount(it)
            showAccountDialog = false
        })
    }

    if (showCategoryDialog) {
        EditCategoryDialog(onDismiss = { showCategoryDialog = false }, onSave = {
            store.addCategory(it)
            showCategoryDialog = false
        })
    }

    if (showOperationDialog) {
        AddOperationDialog(store = store, onDismiss = { showOperationDialog = false })
    }

    if (showTransferDialog) {
        TransferDialog(store = store, onDismiss = { showTransferDialog = false })
    }
}

@Composable
fun OperationsTab(store: FinanceStore) {
    val total = store.accounts.sumOf { it.balance }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Общий баланс: ${"%.2f".format(total)} BYN", style = MaterialTheme.typography.headlineSmall)
        Text("Добавляйте доходы и расходы через кнопку +", color = Color.Gray)

        store.accounts.forEach { account ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(18.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(account.color)
                        )
                        Text(account.name, modifier = Modifier.padding(start = 10.dp))
                    }
                    Text("${"%.2f".format(account.balance)} BYN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HistoryTab(store: FinanceStore) {
    val operations = store.filteredOperations()
    val expenseByCategory = operations
        .filter { it.type == OperationType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { (_, ops) -> ops.sumOf { it.amount } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            HistoryFilter.entries.forEachIndexed { index, filter ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = HistoryFilter.entries.size),
                    onClick = { store.selectedFilter = filter },
                    selected = store.selectedFilter == filter,
                    label = { Text(filter.name.lowercase()) }
                )
            }
        }

        PieChart(
            values = expenseByCategory.values.toList(),
            colors = expenseByCategory.keys.mapNotNull { id -> store.categories.find { it.id == id }?.color }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(operations) { op ->
                val category = store.categories.find { it.id == op.categoryId }
                val account = store.accounts.find { it.id == op.accountId }
                Card {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(category?.name ?: op.type.name)
                            Text(account?.name ?: "", color = Color.Gray)
                        }
                        Text(
                            text = when (op.type) {
                                OperationType.INCOME -> "+${"%.2f".format(op.amount)}"
                                OperationType.EXPENSE -> "-${"%.2f".format(op.amount)}"
                                OperationType.TRANSFER -> "↔ ${"%.2f".format(op.amount)}"
                            } + " BYN"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(values: List<Double>, colors: List<Color>) {
    val sum = values.sum().takeIf { it > 0 } ?: return
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        var startAngle = -90f
        values.forEachIndexed { index, value ->
            val angle = (value / sum * 360).toFloat()
            drawArc(
                color = colors.getOrElse(index) { Color.LightGray },
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = true
            )
            startAngle += angle
        }

        drawCircle(color = Color.White, radius = size.minDimension * 0.3f)
    }
}

@Composable
fun AccountsTab(store: FinanceStore) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(store.accounts) { account ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(account.name)
                        Text("${"%.2f".format(account.balance)} BYN", color = Color.Gray)
                    }
                    IconButton(onClick = { store.deleteAccount(account.id) }) { Text("Удалить") }
                }
            }
        }
    }
}

@Composable
fun CategoriesTab(store: FinanceStore) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(store.categories) { category ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(category.name)
                        Text(category.operationType.name, color = Color.Gray)
                    }
                    IconButton(onClick = { store.deleteCategory(category.id) }) { Text("Удалить") }
                }
            }
        }
    }
}

@Composable
fun EditAccountDialog(onDismiss: () -> Unit, onSave: (Account) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }
    var iconIndex by remember { mutableStateOf(0) }
    var colorIndex by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Новый счёт")
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Стартовый баланс") })

                Text("Иконка")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableIcons.take(4).forEachIndexed { idx, icon ->
                        AssistChip(
                            onClick = { iconIndex = idx },
                            label = { Icon(icon, contentDescription = null) }
                        )
                    }
                }

                Text("Цвет")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableColors.take(4).forEachIndexed { idx, color ->
                        Box(
                            Modifier
                                .size(24.dp)
                                .background(color)
                                .clip(MaterialTheme.shapes.small)
                                .padding(1.dp)
                        )
                        Button(onClick = { colorIndex = idx }) { Text((idx + 1).toString()) }
                    }
                }

                Button(onClick = {
                    onSave(
                        Account(
                            name = name.ifBlank { "Счёт" },
                            icon = availableIcons[iconIndex],
                            color = availableColors[colorIndex],
                            balance = balance.toDoubleOrNull() ?: 0.0
                        )
                    )
                }) { Text("Сохранить") }
            }
        }
    }
}

@Composable
fun EditCategoryDialog(onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember { mutableStateOf("") }
    var iconIndex by remember { mutableStateOf(0) }
    var colorIndex by remember { mutableStateOf(0) }
    var type by remember { mutableStateOf(OperationType.EXPENSE) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Новая категория")
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { type = OperationType.EXPENSE }) { Text("Расход") }
                    Button(onClick = { type = OperationType.INCOME }) { Text("Доход") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableIcons.take(4).forEachIndexed { idx, icon ->
                        AssistChip(onClick = { iconIndex = idx }, label = { Icon(icon, contentDescription = null) })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableColors.take(4).forEachIndexed { idx, _ ->
                        Button(onClick = { colorIndex = idx }) { Text((idx + 1).toString()) }
                    }
                }

                Button(onClick = {
                    onSave(
                        Category(
                            name = name.ifBlank { "Категория" },
                            icon = availableIcons[iconIndex],
                            color = availableColors[colorIndex],
                            operationType = type
                        )
                    )
                }) { Text("Сохранить") }
            }
        }
    }
}

@Composable
fun AddOperationDialog(store: FinanceStore, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(store.accounts.firstOrNull()?.id.orEmpty()) }
    var categoryId by remember { mutableStateOf(store.categories.firstOrNull()?.id.orEmpty()) }
    var type by remember { mutableStateOf(OperationType.EXPENSE) }
    var dayOffset by remember { mutableStateOf(0L) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Новая операция")
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Сумма") })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { type = OperationType.EXPENSE }) { Text("Расход") }
                    Button(onClick = { type = OperationType.INCOME }) { Text("Доход") }
                }

                Text("Счёт")
                store.accounts.forEach { account ->
                    AssistChip(onClick = { accountId = account.id }, label = { Text(account.name) })
                }

                Text("Категория")
                store.categories.filter { it.operationType == type }.forEach { category ->
                    AssistChip(onClick = { categoryId = category.id }, label = { Text(category.name) })
                }

                Text("Дата")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { dayOffset = 0 }, label = { Text("Сегодня") })
                    AssistChip(onClick = { dayOffset = 1 }, label = { Text("Вчера") })
                    AssistChip(onClick = { dayOffset = 2 }, label = { Text("Позавчера") })
                }

                Button(onClick = {
                    store.addOperation(
                        FinanceOperation(
                            type = type,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            accountId = accountId,
                            categoryId = categoryId,
                            date = LocalDate.now().minusDays(dayOffset)
                        )
                    )
                    onDismiss()
                }) { Text("Сохранить") }
            }
        }
    }
}

@Composable
fun TransferDialog(store: FinanceStore, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var from by remember { mutableStateOf(store.accounts.firstOrNull()?.id.orEmpty()) }
    var to by remember { mutableStateOf(store.accounts.getOrNull(1)?.id ?: from) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Перевод между счетами")
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Сумма") })
                Text("Откуда")
                store.accounts.forEach { account ->
                    AssistChip(onClick = { from = account.id }, label = { Text(account.name) })
                }
                Text("Куда")
                store.accounts.forEach { account ->
                    AssistChip(onClick = { to = account.id }, label = { Text(account.name) })
                }

                Button(onClick = {
                    store.addOperation(
                        FinanceOperation(
                            type = OperationType.TRANSFER,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            accountId = from,
                            categoryId = null,
                            sourceAccountId = from,
                            targetAccountId = to,
                            date = LocalDate.now()
                        )
                    )
                    onDismiss()
                }) { Text("Перевести") }
            }
        }
    }
}
