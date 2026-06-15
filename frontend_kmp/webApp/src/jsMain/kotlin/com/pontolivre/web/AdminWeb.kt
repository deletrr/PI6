package com.pontolivre.web

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import com.pontolivre.shared.viewmodel.*
import com.pontolivre.shared.repository.AppSession
import com.pontolivre.shared.model.*
import com.pontolivre.shared.util.format
import com.pontolivre.shared.api.ApiClient
import com.pontolivre.shared.api.ApiResult
import com.pontolivre.shared.api.toResult
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.delay
import kotlinx.browser.window

@Composable
fun AdminLayoutWeb(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val user by AppSession.currentUser.collectAsState()

    Div({
        style {
            display(DisplayStyle.Flex)
            minHeight(100.vh)
            backgroundColor(Color("#f8f9fa"))
            property("font-family", "'Inter', sans-serif")
        }
    }) {
        // SIDEBAR
        Nav({
            style {
                width(260.px)
                backgroundColor(Color("#1e293b"))
                color(Color.white)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                padding(20.px)
                property("flex-shrink", "0")
            }
        }) {
            H2({ style { margin(0.px); marginBottom(40.px); fontSize(24.px); color(Color("#38bdf8")) } }) {
                Text("PontoLivre")
            }

            Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px); flex(1) } }) {
                SidebarItem("Dashboard", "dashboard", currentScreen == "dashboard", onNavigate)
                SidebarItem("Usuários", "users", currentScreen == "users", onNavigate)
                SidebarItem("Parquímetros", "meters", currentScreen == "meters", onNavigate)
                SidebarItem("Sessões", "sessions", currentScreen == "sessions", onNavigate)
                SidebarItem("Receita", "extract", currentScreen == "extract", onNavigate)
                SidebarItem("Obstruções", "obstructed", currentScreen == "obstructed", onNavigate)
                SidebarItem("Logs MQTT", "logs", currentScreen == "logs", onNavigate)
            }

            // User info at bottom
            Div({
                style {
                    property("border-top", "1px solid rgba(255,255,255,0.1)")
                    paddingTop(20.px)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.SpaceBetween)
                }
            }) {
                Div {
                    Div({ style { fontSize(14.px); fontWeight("bold") } }) { Text(user?.name ?: "Admin") }
                    Div({ style { fontSize(12.px); opacity(0.6) } }) { Text("Administrador") }
                }
                Button({
                    onClick { AppSession.logout(); onLogout() }
                    style {
                        backgroundColor(Color.transparent); color(Color("#ef4444")); border(0.px); cursor("pointer")
                    }
                }) { Text("Sair") }
            }
        }

        // MAIN CONTENT
        Div({
            style {
                flex(1)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                maxHeight(100.vh)
            }
        }) {
            // Header
            Header({
                style {
                    height(70.px)
                    backgroundColor(Color.white)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    padding(0.px, 40.px)
                    property("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                    property("flex-shrink", "0")
                }
            }) {
                H3({ style { margin(0.px); color(Color("#1e293b")) } }) {
                    val title = when(currentScreen) {
                        "dashboard" -> "Dashboard"
                        "users" -> "Usuários"
                        "meters" -> "Parquímetros"
                        "sessions" -> "Histórico de Sessões"
                        "active_sessions" -> "Sessões Ativas"
                        "extract" -> "Extrato Financeiro"
                        "obstructed" -> "Vagas Obstruídas"
                        "logs" -> "Logs do Sistema"
                        else -> currentScreen.replace("_", " ").uppercase()
                    }
                    Text(title)
                }
            }

            // Page Content
            Main({
                style {
                    padding(40.px)
                    property("overflow-y", "auto")
                    flex(1)
                }
            }) {
                content()
            }
        }
    }
}

@Composable
fun SidebarItem(label: String, route: String, active: Boolean, onNavigate: (String) -> Unit) {
    Div({
        onClick { onNavigate(route) }
        style {
            padding(12.px, 16.px)
            borderRadius(8.px)
            cursor("pointer")
            if (active) {
                backgroundColor(Color("#334155"))
                color(Color("#38bdf8"))
            } else {
                property("transition", "all 0.2s")
            }
        }
    }) {
        Text(label)
    }
}

@Composable
fun AdminDashboardWeb(viewModel: AdminDashboardViewModel, onNavigate: (String) -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
        while(true) { delay(10000); viewModel.load() }
    }

    state.dashboard?.let { d ->
        Div({
            style {
                display(DisplayStyle.Grid)
                gridTemplateColumns("repeat(auto-fit, minmax(240.px, 1fr))")
                gap(24.px)
            }
        }) {
            ModernStatCard("Total Usuários", d.totalUsers.toString(), "#6366f1") { onNavigate("users") }
            ModernStatCard("Vagas Disponíveis", "${d.freeMeters}/${d.totalMeters}", "#22c55e") { onNavigate("meters") }
            ModernStatCard("Sessões Ativas", d.activeSessions.toString(), "#f59e0b") { onNavigate("active_sessions") }
            ModernStatCard("Receita (Hoje)", "R$ ${d.todayRevenue}", "#10b981") { onNavigate("extract") }
            
            val obstructed = (d.occupiedMeters - d.activeSessions).coerceAtLeast(0)
            ModernStatCard("Vagas Obstruídas", obstructed.toString(), "#ef4444") { onNavigate("obstructed") }
            ModernStatCard("Logs MQTT", "Acessar Logs", "#ec4899") { onNavigate("logs") }
        }
    }
}

@Composable
fun ModernStatCard(label: String, value: String, accentColor: String, onClick: () -> Unit) {
    Div({
        onClick { onClick() }
        style {
            backgroundColor(Color.white); padding(24.px); borderRadius(12.px)
            property("box-shadow", "0 1px 3px rgba(0,0,0,0.1)"); position(Position.Relative); property("overflow", "hidden")
            cursor("pointer")
            display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); justifyContent(JustifyContent.Center)
            minHeight(120.px)
        }
    }) {
        Div({ style { width(4.px); height(100.percent); backgroundColor(Color(accentColor)); position(Position.Absolute); left(0.px); top(0.px) } })
        Div {
            Div({ style { fontSize(14.px); color(Color("#64748b")); fontWeight(500) } }) { Text(label) }
            Div({ style { fontSize(28.px); fontWeight(700); color(Color("#1e293b")); marginTop(8.px) } }) { Text(value) }
        }
    }
}

@Composable
fun UsersListWeb(viewModel: AdminUsersViewModel) {
    val state by viewModel.state.collectAsState()
    var editUser by remember { mutableStateOf<UserModel?>(null) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }

    CardWrapper {
        Div({ style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); marginBottom(24.px) } }) {
            H3({ style { margin(0.px) } }) { Text("Lista de Usuários") }
            Input(InputType.Text) {
                value(search)
                onInput { search = it.value; viewModel.load(it.value) }
                placeholder("Pesquisar...")
                style { padding(8.px, 16.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#e2e8f0")); width(300.px) }
            }
        }

        if (state.isLoading) Text("Carregando...")
        else {
            ModernTable {
                Thead {
                    Tr {
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Nome") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("E-mail") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Saldo") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Nível") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Status") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Ações") }
                    }
                }
                Tbody {
                    state.users.forEach { user ->
                        Tr({ style { property("border-bottom", "1px solid #f1f5f9") } }) {
                            Td({ style { padding(12.px) } }) { Text(user.name) }
                            Td({ style { padding(12.px) } }) { Text(user.email) }
                            Td({ style { padding(12.px) } }) { Text("R$ ${user.balance}") }
                            Td({ style { padding(12.px) } }) { 
                                val roleLabel = if (user.role == "ADMIN") "Administrador" else "Usuário"
                                Span({
                                    style {
                                        padding(4.px, 8.px); borderRadius(4.px); fontSize(12.px); fontWeight("bold")
                                        if (user.role == "ADMIN") { backgroundColor(Color("#fee2e2")); color(Color("#991b1b")) }
                                        else { backgroundColor(Color("#f1f5f9")); color(Color("#475569")) }
                                    }
                                }) { Text(roleLabel) }
                            }
                            Td({ style { padding(12.px) } }) {
                                Span({
                                    style {
                                        width(8.px); height(8.px); borderRadius(50.percent); display(DisplayStyle.InlineBlock); marginRight(8.px)
                                        backgroundColor(if (user.active) Color("#22c55e") else Color("#94a3b8"))
                                    }
                                })
                                Text(if (user.active) "Ativo" else "Inativo")
                            }
                            Td({ style { padding(12.px) } }) {
                                Button({
                                    onClick { editUser = user }
                                    style { color(Color("#6366f1")); backgroundColor(Color.transparent); border(0.px); cursor("pointer"); fontWeight(600) }
                                }) { Text("Editar") }
                            }
                        }
                    }
                }
            }
        }
    }

    editUser?.let { user ->
        WebModal("Editar Usuário: ${user.name}", onDismiss = { editUser = null }) {
            var balance by remember { mutableStateOf(user.balance.toString()) }
            var role by remember { mutableStateOf(user.role) }
            var active by remember { mutableStateOf(user.active) }

            Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(16.px) } }) {
                Div {
                    Label(forId = "bal") { Text("Saldo disponível") }
                    Input(InputType.Text) {
                        id("bal")
                        value(balance)
                        onInput { balance = it.value }
                        style { width(100.percent); padding(10.px); marginTop(4.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#cbd5e1")) }
                    }
                }
                Div {
                    Label { Text("Nível de Acesso") }
                    Select({
                        onInput { role = it.value ?: "USER" }
                        style { width(100.percent); padding(10.px); marginTop(4.px); borderRadius(6.px) }
                    }) {
                        Option("USER", { if (role == "USER") selected() }) { Text("Usuário Comum") }
                        Option("ADMIN", { if (role == "ADMIN") selected() }) { Text("Administrador") }
                    }
                }
                Div({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px) } }) {
                    CheckboxInput(active) { onChange { active = it.value } }
                    Label { Text("Conta Ativa") }
                }

                Button({
                    onClick {
                        viewModel.updateUser(user.id, AdminUpdateUserRequest(
                            balance = balance.toDoubleOrNull() ?: 0.0,
                            role = role,
                            active = active
                        )) { editUser = null }
                    }
                    style { 
                        padding(12.px); backgroundColor(Color("#6366f1")); color(Color.white); border(0.px); borderRadius(8.px); cursor("pointer"); fontWeight(600) 
                    }
                }) { Text("Salvar Alterações") }
            }
        }
    }
}

@Composable
fun MetersListWeb(viewModel: AdminMetersViewModel) {
    val state by viewModel.state.collectAsState()
    var editingMeter by remember { mutableStateOf<ParkingMeterModel?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    CardWrapper {
        Div({ style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); marginBottom(24.px) } }) {
            H3({ style { margin(0.px) } }) { Text("Parquímetros") }
            Button({
                onClick { showCreate = true }
                style { 
                    padding(10.px, 20.px); backgroundColor(Color("#10b981")); color(Color.white)
                    border(0.px); borderRadius(8.px); cursor("pointer"); fontWeight(600) 
                }
            }) { Text("+ Novo Dispositivo") }
        }

        ModernTable {
            Thead {
                Tr {
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Código") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Status") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Descrição") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Localização") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Ações") }
                }
            }
            Tbody {
                state.meters.forEach { meter ->
                    Tr({ style { property("border-bottom", "1px solid #f1f5f9") } }) {
                        Td({ style { padding(12.px) } }) { B { Text(meter.code) } }
                        Td({ style { padding(12.px) } }) {
                            val statusLabel = when(meter.status) {
                                "FREE" -> "Livre"
                                "OCCUPIED" -> "Ocupada"
                                else -> meter.status
                            }
                            val statusColor = when(meter.status) {
                                "FREE" -> "#22c55e"
                                "OCCUPIED" -> "#ef4444"
                                else -> "#64748b"
                            }
                            Span({
                                style {
                                    padding(4.px, 8.px); borderRadius(4.px); fontSize(12.px); fontWeight("bold")
                                    backgroundColor(Color(statusColor)); color(Color.white)
                                }
                            }) { Text(statusLabel) }
                        }
                        Td({ style { padding(12.px) } }) { Text(meter.description ?: "-") }
                        Td({ style { padding(12.px) } }) { Text(if (meter.latitude != null) "${meter.latitude}, ${meter.longitude}" else "Não configurado") }
                        Td({ style { padding(12.px) } }) {
                            Button({
                                onClick { editingMeter = meter }
                                style { color(Color("#6366f1")); backgroundColor(Color.transparent); border(0.px); cursor("pointer"); fontWeight(600) }
                            }) { Text("Editar") }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        WebModal("Novo Parquímetro", onDismiss = { showCreate = false }) {
            var code by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }
            
            Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(12.px) } }) {
                Input(InputType.Text) {
                    placeholder("Código (ex: PKM-001)")
                    value(code)
                    onInput { code = it.value }
                    style { padding(10.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#cbd5e1")) }
                }
                Input(InputType.Text) {
                    placeholder("Descrição")
                    value(desc)
                    onInput { desc = it.value }
                    style { padding(10.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#cbd5e1")) }
                }
                Button({
                    onClick {
                        viewModel.createMeter(CreateParkingMeterRequest(code, desc)) { showCreate = false }
                    }
                    style { padding(12.px); backgroundColor(Color("#10b981")); color(Color.white); border(0.px); borderRadius(6.px); cursor("pointer") }
                }) { Text("Criar Parquímetro") }
            }
        }
    }

    editingMeter?.let { meter ->
        WebModal("Editar ${meter.code}", onDismiss = { editingMeter = null }) {
            var desc by remember { mutableStateOf(meter.description ?: "") }
            var lat by remember { mutableStateOf(meter.latitude?.toString() ?: "") }
            var lng by remember { mutableStateOf(meter.longitude?.toString() ?: "") }
            var active by remember { mutableStateOf(meter.active) }

            Div({ style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(16.px) } }) {
                Div {
                    Label { Text("Descrição / Endereço") }
                    Input(InputType.Text) {
                        value(desc)
                        onInput { desc = it.value }
                        style { width(100.percent); padding(10.px); marginTop(4.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#cbd5e1")) }
                    }
                }
                Div({ style { display(DisplayStyle.Flex); gap(8.px) } }) {
                    Div({ style { flex(1) } }) {
                        Label { Text("Lat") }
                        Input(InputType.Text) {
                            value(lat)
                            onInput { lat = it.value }
                            style { width(100.percent); padding(10.px); marginTop(4.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#cbd5e1")) }
                        }
                    }
                    Div({ style { flex(1) } }) {
                        Label { Text("Lng") }
                        Input(InputType.Text) {
                            value(lng)
                            onInput { lng = it.value }
                            style { width(100.percent); padding(10.px); marginTop(4.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#cbd5e1")) }
                        }
                    }
                }
                Div({ style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px) } }) {
                    CheckboxInput(active) { onChange { active = it.value } }
                    Label { Text("Dispositivo Ativo") }
                }

                Button({
                    onClick {
                        viewModel.updateMeter(meter.id, UpdateParkingMeterRequest(
                            description = desc, 
                            latitude = lat.toDoubleOrNull(), 
                            longitude = lng.toDoubleOrNull(), 
                            active = active
                        )) { editingMeter = null }
                    }
                    style { 
                        padding(12.px); backgroundColor(Color("#6366f1")); color(Color.white); border(0.px); borderRadius(8.px); cursor("pointer"); fontWeight(600) 
                    }
                }) { Text("Salvar Alterações") }
            }
        }
    }
}

@Composable
fun SessionsListWeb(viewModel: AdminSessionsViewModel) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    val filtered = state.sessions.filter { s ->
        (selectedStatus == null || s.status == selectedStatus) &&
        (searchQuery.isBlank() || s.meterCode.contains(searchQuery, true) || s.userName.contains(searchQuery, true))
    }

    CardWrapper {
        Div({ style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); marginBottom(24.px) } }) {
            H3({ style { margin(0.px) } }) { Text("Histórico de Sessões") }
            Div({ style { display(DisplayStyle.Flex); gap(12.px) } }) {
                Select({
                    onInput { selectedStatus = it.value?.takeIf { it != "ALL" } }
                    style { padding(8.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#e2e8f0")) }
                }) {
                    Option("ALL") { Text("Todos Status") }
                    Option("ACTIVE") { Text("Ativa") }
                    Option("CLOSED") { Text("Encerrada") }
                    Option("OVERTIME") { Text("Excedida") }
                    Option("PENDING") { Text("Pendente") }
                    Option("EXPIRED") { Text("Expirada") }
                }
                Input(InputType.Text) {
                    value(searchQuery)
                    onInput { searchQuery = it.value }
                    placeholder("Buscar...")
                    style { padding(8.px, 16.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#e2e8f0")) }
                }
            }
        }

        ModernTable {
            Thead {
                Tr {
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Usuário") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Vaga") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Início") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Valor") }
                    Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Status") }
                }
            }
            Tbody {
                filtered.forEach { session ->
                    Tr({ style { property("border-bottom", "1px solid #f1f5f9") } }) {
                        Td({ style { padding(12.px) } }) { Text(session.userName) }
                        Td({ style { padding(12.px) } }) { B { Text(session.meterCode) } }
                        Td({ style { padding(12.px) } }) { Text(session.startTime.replace("T", " ").take(16)) }
                        Td({ style { padding(12.px) } }) { Text("R$ ${session.amountCharged}") }
                        Td({ style { padding(12.px) } }) {
                            val statusLabel = when(session.status) {
                                "ACTIVE" -> "Ativa"
                                "CLOSED" -> "Encerrada"
                                "OVERTIME" -> "Excedida"
                                "PENDING" -> "Pendente"
                                "EXPIRED" -> "Expirada"
                                else -> session.status
                            }
                            Text(statusLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObstructedWeb(meterVm: AdminMetersViewModel, sessionVm: AdminSessionsViewModel) {
    val meterState by meterVm.state.collectAsState()
    val sessionState by sessionVm.state.collectAsState()

    LaunchedEffect(Unit) {
        meterVm.load()
        sessionVm.load()
    }

    val activeMeterCodes = sessionState.sessions.filter { it.status == "ACTIVE" }.map { it.meterCode }
    val obstructed = meterState.meters.filter { it.status == "OCCUPIED" && !activeMeterCodes.contains(it.code) }

    Div({
        style {
            display(DisplayStyle.Grid)
            gridTemplateColumns("repeat(auto-fill, minmax(300.px, 1fr))")
            gap(24.px)
        }
    }) {
        obstructed.forEach { meter ->
            CardWrapper {
                Div({ style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center) } }) {
                    Div {
                        Div({ style { fontSize(20.px); fontWeight(700); color(Color("#1e293b")) } }) { Text(meter.code) }
                        Div({ style { fontSize(14.px); color(Color("#64748b")); marginTop(4.px) } }) { Text(meter.description ?: "Sem descrição") }
                    }
                    Span({
                        style {
                            padding(4.px, 8.px); borderRadius(4.px); fontSize(11.px); fontWeight("bold")
                            backgroundColor(Color("#fee2e2")); color(Color("#ef4444"))
                        }
                    }) { Text("SEM SESSÃO") }
                }
                Button({
                    onClick { if (window.confirm("Acionar fiscal para ${meter.code}?")) window.alert("Fiscal enviado!") }
                    style {
                        marginTop(20.px); width(100.percent); padding(12.px); backgroundColor(Color("#ef4444")); color(Color.white)
                        border(0.px); borderRadius(8.px); fontWeight(600); cursor("pointer")
                    }
                }) { Text("ACIONAR FISCAL") }
            }
        }
    }
    if (obstructed.isEmpty()) Text("Nenhuma obstrução detectada no momento.")
}

@Composable
fun LogsWeb() {
    var logs by remember { mutableStateOf<List<MqttLogModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val fetchLogs = suspend {
        // isLoading apenas no primeiro carregamento
        if (logs.isEmpty()) isLoading = true
        
        try {
            // Chamada ao endpoint real de logs
            when (val r = ApiClient.httpClient.get { url("/api/admin/mqtt-logs") }.toResult<List<MqttLogModel>>()) {
                is ApiResult.Success -> {
                    // Lógica de Fila: Pega os últimos 100 itens
                    logs = r.data.take(100)
                }
                else -> {}
            }
        } catch (e: Exception) {
            // Silencioso no polling
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        fetchLogs()
        while(true) {
            delay(5000) // Atualiza a cada 5 segundos
            fetchLogs()
        }
    }

    CardWrapper {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(16.px)
            }
        }) {
            H3({ style { margin(0.px) } }) { Text("Logs do Sistema (MQTT)") }
            Span({ 
                style { 
                    fontSize(12.px); color(Color("#64748b")); backgroundColor(Color("#f1f5f9"))
                    padding(4.px, 8.px); borderRadius(4.px)
                } 
            }) { Text("Atualizando em tempo real (Fila: 100)") }
        }

        if (isLoading) Text("Carregando logs...")
        else {
            Div({
                style {
                    backgroundColor(Color("#0f172a")); color(Color("#38bdf8")); padding(20.px)
                    borderRadius(8.px); property("font-family", "'JetBrains Mono', monospace")
                    fontSize(13.px); property("line-height", "1.6")
                    minHeight(450.px); maxHeight(600.px); property("overflow-y", "auto")
                }
            }) {
                if (logs.isEmpty()) {
                    Div { Text("> Aguardando mensagens do servidor...") }
                } else {
                    logs.forEach { log ->
                        Div({ style { marginBottom(4.px); property("border-bottom", "1px solid rgba(56, 189, 248, 0.1)") } }) {
                            Span({ style { color(Color("#94a3b8")) } }) { Text("${log.createdAt.replace("T", " ").take(19)} ") }
                            Span({ style { fontWeight("bold"); color(Color("#f472b6")) } }) { Text("[${log.meterCode ?: "IOT"}] ") }
                            Text(log.payload)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSessionsWeb(viewModel: AdminSessionsViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    CardWrapper {
        H3({ style { marginBottom(24.px) } }) { Text("Sessões Ativas") }
        val active = state.sessions.filter { it.status == "ACTIVE" }
        
        if (state.isLoading) Text("Carregando...")
        else if (active.isEmpty()) Text("Nenhuma sessão ativa.")
        else {
            Div({ style { display(DisplayStyle.Grid); gridTemplateColumns("repeat(auto-fill, minmax(280.px, 1fr))"); gap(16.px) } }) {
                active.forEach { session ->
                    Div({
                        style {
                            padding(16.px); border(1.px, LineStyle.Solid, Color("#e2e8f0")); borderRadius(8.px)
                            display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px)
                        }
                    }) {
                        Div({ style { fontWeight(700); color(Color("#1e293b")) } }) { Text(session.userName) }
                        Div({ style { fontSize(14.px); color(Color("#64748b")) } }) { Text("Vaga: ${session.meterCode}") }
                        Div({ style { fontSize(14.px); color(Color("#64748b")) } }) { Text("Início: ${session.startTime.substringAfter("T").take(5)}h") }
                    }
                }
            }
        }
    }
}

// ── Shared UI Components ──────────────────────────────────────────────────────

@Composable
fun CardWrapper(content: @Composable () -> Unit) {
    Div({
        style {
            backgroundColor(Color.white); padding(24.px); borderRadius(12.px)
            property("box-shadow", "0 1px 3px rgba(0,0,0,0.1)"); position(Position.Relative); property("overflow", "hidden")
        }
    }) {
        content()
    }
}

@Composable
fun ModernTable(content: @Composable () -> Unit) {
    Table({
        style {
            width(100.percent); property("border-collapse", "collapse"); marginTop(10.px)
        }
    }) {
        content()
    }
}

@Composable
fun AdminExtractWeb(viewModel: WalletViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadAdminExtract() }

    CardWrapper {
        H3({ style { marginBottom(24.px) } }) { Text("Extrato Financeiro Geral") }
        
        if (state.isLoading) Text("Carregando...")
        else {
            val credits = state.transactions.filter { it.type.startsWith("CREDIT") }.sumOf { it.amount }
            val debits = state.transactions.filter { it.type.startsWith("DEBIT") }.sumOf { it.amount }

            Div({
                style {
                    display(DisplayStyle.Flex); gap(24.px); marginBottom(32.px)
                }
            }) {
                Div({
                    style {
                        flex(1); padding(20.px); backgroundColor(Color("#f0fdf4")); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#bcf0da"))
                    }
                }) {
                    Div({ style { fontSize(14.px); color(Color("#15803d")) } }) { Text("Total Entradas") }
                    Div({ style { fontSize(24.px); fontWeight("bold"); color(Color("#16a34a")) } }) { Text("R$ ${"%.2f".format(credits).replace(".", ",")}") }
                }
                Div({
                    style {
                        flex(1); padding(20.px); backgroundColor(Color("#fef2f2")); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#fecaca"))
                    }
                }) {
                    Div({ style { fontSize(14.px); color(Color("#b91c1c")) } }) { Text("Total Saídas") }
                    Div({ style { fontSize(24.px); fontWeight("bold"); color(Color("#dc2626")) } }) { Text("R$ ${"%.2f".format(debits).replace(".", ",")}") }
                }
            }

            ModernTable {
                Thead {
                    Tr {
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Data") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Descrição") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Tipo") }
                        Th({ style { textAlign("left"); padding(12.px); property("border-bottom", "2px solid #f1f5f9") } }) { Text("Valor") }
                    }
                }
                Tbody {
                    state.transactions.forEach { tx ->
                        Tr({ style { property("border-bottom", "1px solid #f1f5f9") } }) {
                            Td({ style { padding(12.px) } }) { Text(tx.createdAt.take(10)) }
                            Td({ style { padding(12.px) } }) { Text(tx.description) }
                            Td({ style { padding(12.px) } }) { 
                                val typeLabel = when(tx.type) {
                                    "CREDIT_PIX" -> "Crédito (Pix)"
                                    "CREDIT_CARD" -> "Crédito (Cartão)"
                                    "DEBIT_SESSION" -> "Débito (Sessão)"
                                    "DEBIT_FINE" -> "Débito (Multa)"
                                    "CREDIT_REFUND" -> "Estorno"
                                    else -> tx.type
                                }
                                Text(typeLabel)
                            }
                            Td({ style { padding(12.px); fontWeight("bold"); color(if (tx.type.startsWith("CREDIT")) Color("green") else Color("red")) } }) {
                                Text("${if (tx.type.startsWith("CREDIT")) "+" else "-"} R$ ${"%.2f".format(tx.amount).replace(".", ",")}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebModal(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Div({
        style {
            position(Position.Fixed); top(0.px); left(0.px); right(0.px); bottom(0.px)
            backgroundColor(Color("rgba(15, 23, 42, 0.5)")); display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center); alignItems(AlignItems.Center); property("z-index", "1000")
        }
        onClick { onDismiss() }
    }) {
        Div({
            style {
                backgroundColor(Color.white); padding(32.px); borderRadius(16.px); width(450.px)
                display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(20.px)
            }
            onClick { it.stopPropagation() }
        }) {
            H3({ style { margin(0.px); color(Color("#1e293b")) } }) { Text(title) }
            content()
            Button({
                onClick { onDismiss() }
                style { alignSelf(AlignSelf.End); padding(8.px, 16.px); backgroundColor(Color("#f1f5f9")); border(0.px); borderRadius(6.px); cursor("pointer") }
            }) { Text("Fechar") }
        }
    }
}
