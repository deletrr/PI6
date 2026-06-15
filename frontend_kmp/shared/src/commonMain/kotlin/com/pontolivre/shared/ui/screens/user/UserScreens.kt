package com.pontolivre.shared.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pontolivre.shared.repository.AppSession
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.ui.theme.*
import com.pontolivre.shared.util.format
import com.pontolivre.shared.viewmodel.*
import kotlinx.coroutines.delay

// ── FinesScreen ───────────────────────────────────────────────────────────────

@Composable
fun FinesScreen(viewModel: FinesViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Minhas Multas", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
        } else if (state.fines.isEmpty()) {
            EmptyState("Nenhuma multa registrada.", Icons.Default.CheckCircle)
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.fines) { fine ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null,
                                        tint = WarningAmber, modifier = Modifier.size(20.dp))
                                    Text("Multa – ${fine.meterCode}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold)
                                }
                                StatusBadge(fine.status)
                            }
                            Text(fine.reason, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("R$ ${"%.2f".format(fine.amount)}",
                                    fontWeight = FontWeight.Bold, color = ErrorRed)
                                Text(fine.createdAt.take(10),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── SupportScreen ─────────────────────────────────────────────────────────────

@Composable
fun SupportScreen(viewModel: SupportViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showNewTicket by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Suporte", onNavigateUp = onNavigateUp) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewTicket = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo chamado")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.successMessage?.let {
                Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f))) {
                    Row(Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Text(it, color = SuccessGreen)
                    }
                }
            }

            if (state.isLoading) {
                FullScreenLoading()
            } else if (state.tickets.isEmpty()) {
                EmptyState("Nenhum chamado aberto.", Icons.Default.SupportAgent)
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.tickets) { ticket ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(ticket.subject,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f))
                                    StatusBadge(if (ticket.resolved) "CLOSED" else "ACTIVE")
                                }
                                Text(ticket.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2)
                                if (ticket.response != null) {
                                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                    Row(verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Reply, contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary)
                                        Text(ticket.response,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Text(ticket.createdAt.take(10),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewTicket) {
        AlertDialog(
            onDismissRequest = { showNewTicket = false; subject = ""; message = "" },
            title = { Text("Novo chamado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = subject, onValueChange = { subject = it },
                        label = { Text("Assunto") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = message, onValueChange = { message = it },
                        label = { Text("Mensagem") }, modifier = Modifier.fillMaxWidth(),
                        minLines = 4, maxLines = 6)
                    state.error?.let { ErrorMessage(it) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendTicket(subject, message) {
                            showNewTicket = false; subject = ""; message = ""
                        }
                    },
                    enabled = !state.isSending
                ) {
                    if (state.isSending) CircularProgressIndicator(Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Enviar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewTicket = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── HistoryScreen ─────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(viewModel: SessionViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
        // Polling de histórico opcional, mas vamos atualizar a cada 30s
        while(true) {
            delay(30_000)
            viewModel.loadHistory()
        }
    }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Histórico", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (state.history.isEmpty()) {
            EmptyState("Nenhuma sessão no histórico.", Icons.Default.History)
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.history) { session ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(session.meterCode,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold)
                                StatusBadge(session.status)
                            }
                            session.meterDescription?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider()
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Início", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(session.startTime.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                if (session.endTime != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Fim", style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(session.endTime.take(16).replace("T", " "),
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("${session.chargedHours}h cobrada(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("R$ ${"%.2f".format(session.amountCharged)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (session.amountCharged > 0) ErrorRed
                                            else SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── ProfileScreen ─────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    onNavigateToFines: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateUp: () -> Unit,
    onLogout: () -> Unit
) {
    val user by AppSession.currentUser.collectAsState()

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Meu Perfil", onNavigateUp = onNavigateUp) }
    ) { padding ->
        user?.let { u ->
            Column(
                Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ... (avatar and info code same as before)
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(u.name.first().uppercaseChar().toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Text(u.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(u.email, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Saldo movido para o topo
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saldo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("R$ ${"%.2f".format(u.balance)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("CPF", u.cpf, Icons.Default.Badge)
                        u.phone?.let { InfoRow("Telefone", it, Icons.Default.Phone) }
                    }
                }

                // MENU DE OPÇÕES (Multas e Suporte movidos para cá)
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Minhas Multas") },
                            leadingContent = { Icon(Icons.Default.Warning, null, tint = WarningAmber) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                            modifier = Modifier.clickable(onClick = onNavigateToFines)
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            headlineContent = { Text("Suporte") },
                            leadingContent = { Icon(Icons.Default.SupportAgent, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                            modifier = Modifier.clickable(onClick = onNavigateToSupport)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                // ... logout button same

                OutlinedButton(
                    onClick = { AppSession.logout(); onLogout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sair da conta")
                }
            }
        }
    }
}
