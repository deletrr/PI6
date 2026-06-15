package com.pontolivre.shared.ui.screens.user

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.ui.theme.*
import com.pontolivre.shared.util.format
import com.pontolivre.shared.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

@Composable
fun ActiveSessionScreen(
    viewModel: SessionViewModel,
    onShowNotification: (String, String) -> Unit,
    onNavigateUp: () -> Unit,
    onSessionEnded: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showEndConfirm by remember { mutableStateOf(false) }
    var showPayConfirm by remember { mutableStateOf(false) }
    
    // Flag to detect external termination
    var isExternallyEnded by remember { mutableStateOf(false) }

    // Local elapsed timer — updates every second
    var elapsedSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(state.activeSession) {
        if (state.activeSession == null && !state.isLoading && elapsedSeconds > 0) {
            // Session was active but now is null -> terminated by hardware or admin
            isExternallyEnded = true
        }

        val session = state.activeSession ?: return@LaunchedEffect
        elapsedSeconds = (session.elapsedMinutes ?: 0L) * 60
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadActiveSession()
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null && state.activeSession == null) {
            onSessionEnded()
        }
    }

    var notified15min by remember { mutableStateOf(false) }
    var notified5minTolerance by remember { mutableStateOf(false) }
    var notifiedSecondHourTolerance by remember { mutableStateOf(false) }
    var notified15minPaidHour by remember { mutableStateOf(false) }

    LaunchedEffect(elapsedSeconds) {
        val minutes = elapsedSeconds / 60
        val chargedHours = state.activeSession?.chargedHours ?: 0

        // Alerta de 15 minutos para as 2 horas (120 - 15 = 105)
        if (minutes >= 105 && !notified15min) {
            onShowNotification("Tempo de Estacionamento", "Faltam 15 minutos para atingir o limite de 2 horas!")
            notified15min = true
        }
        // Alerta de 5 minutos para a tolerância (15 - 5 = 10)
        if (minutes >= 10 && minutes < 15 && !notified5minTolerance) {
            onShowNotification("Tolerância", "Faltam 5 minutos para acabar a sua tolerância gratuita!")
            notified5minTolerance = true
        }
        // Alerta de 15 min de tolerância para pagar a segunda hora (aos 60 min)
        if (minutes >= 60 && chargedHours < 2 && !notifiedSecondHourTolerance) {
            onShowNotification("Segunda Hora", "Você entrou na segunda hora. Você tem 15 minutos de tolerância para pagar a segunda hora!")
            notifiedSecondHourTolerance = true
        }

        // NOVO: Alerta quando faltar 15 min para acabar a PRIMEIRA hora paga (se pagou apenas 1h)
        // 1h = 60 min. Alerta aos 45 min.
        if (chargedHours == 1 && minutes >= 45 && minutes < 60 && !notified15minPaidHour) {
            onShowNotification("Tempo de Estacionamento", "Faltam 15 minutos para acabar sua 1ª hora paga!")
            notified15minPaidHour = true
        }
    }

    Scaffold(
        topBar = {
            PontoLivreTopBar(title = "Sessão Ativa", onNavigateUp = onNavigateUp)
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading && !isExternallyEnded) {
                FullScreenLoading()
            } else if (isExternallyEnded) {
                // Feedback de sessão finalizada automaticamente (via Hardware)
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SuccessGreen.copy(alpha = 0.1f),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, null, 
                                modifier = Modifier.size(64.dp), tint = SuccessGreen)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Sessão Finalizada", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("O veículo saiu da vaga e o parquímetro encerrou a sessão automaticamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = onSessionEnded,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Ir para Início")
                    }
                }
            } else if (state.activeSession == null) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState("Nenhuma sessão ativa no momento.", Icons.Default.DirectionsCar)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onNavigateUp) { Text("Voltar") }
                }
            } else {
                val session = state.activeSession!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animated pulsing indicator
                    Box(Modifier.height(140.dp), contentAlignment = Alignment.Center) {
                        PulsingRing()
                        Box(
                            Modifier.size(96.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text("EM ANDAMENTO", style = MaterialTheme.typography.labelLarge,
                        color = SuccessGreen, fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp)

                    if ((state.activeSession?.amountCharged ?: 0.0) > 0.0) {
                        Surface(
                            color = SuccessGreen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                Text("PAGAMENTO CONFIRMADO: ${state.activeSession?.chargedHours}H", 
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Timer
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tempo decorrido", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                formatElapsed(elapsedSeconds),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            val freeMinutesLeft = maxOf(0, 15 - (elapsedSeconds / 60).toInt())
                            if (freeMinutesLeft > 0) {
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text("$freeMinutesLeft min de tolerância sem custo",
                                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Cost estimate
                    val minutes = elapsedSeconds / 60
                    val currentCost = when {
                        minutes < 15 -> 0.0
                        minutes < 60 -> 6.50
                        else -> 13.00
                    }

                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Row(Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Custo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("R$ 6,50 por hora iniciada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                            }
                            Text("R$ ${"%.2f".format(currentCost)}",
                                fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }

                    // Session info
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Detalhes da sessão",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            HorizontalDivider()
                            InfoRow("Vaga", session.meterCode, Icons.Default.LocalParking)
                            session.meterDescription?.let {
                                InfoRow("Local", it, Icons.Default.Place)
                            }
                            InfoRow("Início", formatTime(session.startTime), Icons.Default.Schedule)
                            InfoRow("Tolerância até", formatTime(session.freeUntil), Icons.Default.Timer)
                        }
                    }

                    if (state.error != null) {
                        ErrorMessage(state.error!!)
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showPayConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !state.isLoading && (state.activeSession?.chargedHours ?: 0) < 2
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        val text = if ((state.activeSession?.chargedHours ?: 0) == 0) "PAGAR 1 HORA"
                                   else if ((state.activeSession?.chargedHours ?: 0) == 1) "PAGAR MAIS 1 HORA"
                                   else "HORAS PAGAS (MÁX 2H)"
                        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = { showEndConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !state.isEnding
                    ) {
                        if (state.isEnding) {
                            CircularProgressIndicator(Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError)
                        } else {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("FINALIZAR SESSÃO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            if (state.isEnding) LoadingOverlay()
        }
    }

    if (showPayConfirm) {
        ConfirmDialog(
            title = "Confirmar Pagamento",
            message = "Deseja pagar 1 hora de estacionamento (R$ 6,50)? O valor será debitado do seu saldo.",
            confirmLabel = "Pagar",
            onConfirm = {
                showPayConfirm = false
                state.activeSession?.let { viewModel.payHours(it.id) {} }
            },
            onDismiss = { showPayConfirm = false }
        )
    }

    if (showEndConfirm) {
        ConfirmDialog(
            title = "Finalizar sessão?",
            message = "O valor será debitado do seu saldo conforme o tempo de permanência.",
            confirmLabel = "Finalizar",
            onConfirm = {
                showEndConfirm = false
                state.activeSession?.let { viewModel.endSession(it.id, onSessionEnded) }
            },
            onDismiss = { showEndConfirm = false }
        )
    }
}

@Composable
private fun PulsingRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f, label = "scale",
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f, label = "alpha",
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )
    Box(
        Modifier
            .size((96 * scale).dp)
            .clip(CircleShape)
            .background(SuccessGreen.copy(alpha = alpha))
    )
}

private fun formatElapsed(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60

    val hh = h.toString().padStart(2, '0')
    val mm = m.toString().padStart(2, '0')
    val ss = s.toString().padStart(2, '0')

    return if (h > 0) "$hh:$mm:$ss" else "$mm:$ss"
}

private fun formatTime(isoString: String): String {
    return try {
        // "2024-06-03T10:30:00" → "10:30"
        val timePart = isoString.substringAfter("T").take(5)
        timePart
    } catch (e: Exception) { isoString }
}
