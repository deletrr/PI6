package com.pontolivre.shared.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pontolivre.shared.ui.components.PontoLivreTopBar

@Composable
fun ManualCodeScreen(
    onSubmit: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Inserir Código", onNavigateUp = onNavigateUp) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(24.dp))

            Text("Código da vaga", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Digite o código do parquímetro exibido na vaga.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().trim(); error = null },
                label = { Text("Código da vaga") },
                placeholder = { Text("Ex: PKM-001") },
                leadingIcon = { Icon(Icons.Default.LocalParking, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (code.isNotBlank()) onSubmit(code)
                    else error = "Digite o código da vaga."
                }),
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (code.isBlank()) error = "Digite o código da vaga."
                    else onSubmit(code)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Iniciar estacionamento", fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Common codes hint
            Text("Códigos de exemplo para teste:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PKM-001", "PKM-002", "PKM-003").forEach { sample ->
                    SuggestionChip(
                        onClick = { code = sample },
                        label = { Text(sample, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
