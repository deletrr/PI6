package com.pontolivre.shared.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pontolivre.shared.ui.theme.*
import com.pontolivre.shared.util.format

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
fun FullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Box(Modifier.padding(32.dp)) {
                CircularProgressIndicator()
            }
        }
    }
}

// ── Error / Empty ─────────────────────────────────────────────────────────────

@Composable
fun ErrorMessage(message: String, onRetry: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.Warning, contentDescription = null,
            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (onRetry != null) {
            OutlinedButton(onClick = onRetry) { Text("Tentar novamente") }
        }
    }
}

@Composable
fun EmptyState(message: String, icon: ImageVector = Icons.Default.Inbox) {
    Column(
        Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ── Status badge ──────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: String) {
    val color = statusColor(status)
    val label = when (status.uppercase()) {
        "FREE"        -> "Livre"
        "OCCUPIED"    -> "Ocupada"
        "RESERVED"    -> "Reservada"
        "MAINTENANCE" -> "Manutenção"
        "ACTIVE"      -> "Ativa"
        "CLOSED"      -> "Encerrada"
        "OVERTIME"    -> "Excedida"
        "PENDING"     -> "Pendente"
        "PAID"        -> "Paga"
        "DISPUTED"    -> "Contestada"
        else          -> status
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                Modifier.size(8.dp).clip(CircleShape).background(color)
            )
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        action?.invoke()
    }
}

// ── Info row ──────────────────────────────────────────────────────────────────

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── Currency display ──────────────────────────────────────────────────────────

@Composable
fun CurrencyText(value: Double, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge) {
    Text("R$ ${"%.2f".format(value)}", style = style, fontWeight = FontWeight.SemiBold)
}

// ── Pulsing dot for live status ───────────────────────────────────────────────

@Composable
fun PulsingDot(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f, label = "alpha",
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse)
    )
    Box(modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = alpha)))
}

// ── Top app bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PontoLivreTopBar(
    title: String,
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onNavigateUp != null) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ── Confirm dialog ────────────────────────────────────────────────────────────

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(confirmLabel) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
expect fun MiniMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
)
