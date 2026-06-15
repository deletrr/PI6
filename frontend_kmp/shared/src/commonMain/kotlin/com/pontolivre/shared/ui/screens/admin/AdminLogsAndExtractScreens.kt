package com.pontolivre.shared.ui.screens.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pontolivre.shared.api.ApiClient
import com.pontolivre.shared.api.ApiResult
import com.pontolivre.shared.api.toResult
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.ui.theme.*
import com.pontolivre.shared.util.format
import com.pontolivre.shared.viewmodel.WalletViewModel
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.pontolivre.shared.model.*
import kotlinx.serialization.Serializable

data class AdminLogsUiState(
    val logs: List<MqttLogModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminLogsViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminLogsUiState())
    val state: StateFlow<AdminLogsUiState> = _state

    fun load() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val r = ApiClient.httpClient.get("/api/admin/mqtt-logs")
                .toResult<List<MqttLogModel>>()) {
                is ApiResult.Success -> _state.value =
                    _state.value.copy(logs = r.data, isLoading = false)
                is ApiResult.Error   -> _state.value =
                    _state.value.copy(isLoading = false, error = r.message)
            }
        }
    }
}

@Composable
fun AdminLogsScreen(viewModel: AdminLogsViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyErrors by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    
    val filteredLogs = remember(state.logs, searchQuery, showOnlyErrors) {
        state.logs.filter { log ->
            val matchesSearch = searchQuery.isBlank() || 
                log.meterCode?.contains(searchQuery, ignoreCase = true) == true ||
                log.topic.contains(searchQuery, ignoreCase = true)
            
            val matchesError = !showOnlyErrors || !log.processed
            
            matchesSearch && matchesError
        }
    }

    Scaffold(
        topBar = {
            PontoLivreTopBar(
                title = "Logs MQTT",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // BUSCA E FILTROS
            Card(
                Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por ID do parquímetro...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = showOnlyErrors, onCheckedChange = { showOnlyErrors = it })
                        Text("Mostrar apenas falhas/pendentes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            when {
                state.isLoading  -> FullScreenLoading()
                state.error != null -> ErrorMessage(state.error!!, onRetry = { viewModel.load() })
                filteredLogs.isEmpty() -> EmptyState("Nenhum log encontrado.", Icons.Default.Code)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) { items(filteredLogs) { MqttLogRow(it) } }
            }
        }
    }
}

@Composable
private fun MqttLogRow(log: MqttLogModel) {
    val color = when (log.payload.lowercase()) {
        "ocupado" -> ErrorRed
        "livre"   -> SuccessGreen
        else      -> WarningAmber
    }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (log.processed) MaterialTheme.colorScheme.surfaceVariant
                             else WarningAmber.copy(alpha = 0.08f))) {
        Row(Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(log.topic, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text(log.payload,
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = color, fontWeight = FontWeight.Bold)
                    }
                    log.meterCode?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(log.createdAt.take(19).replace("T", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(if (log.processed) Icons.Default.CheckCircle else Icons.Default.AccessTime,
                contentDescription = null,
                tint = if (log.processed) SuccessGreen else WarningAmber,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AdminExtractScreen(viewModel: WalletViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadAdminExtract() }
    Scaffold(
        topBar = {
            PontoLivreTopBar(title = "Extrato Geral", onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = { viewModel.loadAdminExtract() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                })
        }
    ) { padding ->
        when {
            state.isLoading -> FullScreenLoading()
            state.transactions.isEmpty() ->
                EmptyState("Nenhuma transação.", Icons.Default.ReceiptLong)
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val credits = state.transactions
                        .filter { it.type.startsWith("CREDIT") }.sumOf { it.amount }
                    val debits  = state.transactions
                        .filter { it.type.startsWith("DEBIT")  }.sumOf { it.amount }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Entradas", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("R$ ${"%.2f".format(credits)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            VerticalDivider(Modifier.height(40.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Saídas", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("R$ ${"%.2f".format(debits)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                        }
                    }
                }
                items(state.transactions) { tx ->
                    com.pontolivre.shared.ui.screens.user.TransactionRow(tx, onClick = { /* Detalhes não necessários aqui ou implementar rota admin */ })
                }
            }
        }
    }
}
