package com.pontolivre.web

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.css.*
import com.pontolivre.shared.repository.AppSession
import com.pontolivre.shared.viewmodel.*

fun main() {
    AppSession.init()
    
    renderComposable(rootElementId = "root") {
        Style(AppStylesheet)
        
        var currentScreen by remember { mutableStateOf("dashboard") }
        val user by AppSession.currentUser.collectAsState()
        
        if (user == null) {
            val authVm = remember { AuthViewModel() }
            LoginWeb(
                viewModel = authVm,
                onLoginSuccess = { currentScreen = "dashboard" }
            )
        } else if (user?.role != "ADMIN") {
            Div({ classes(AppStylesheet.container) }) {
                H1 { Text("Acesso Negado") }
                P { Text("Esta área é restrita a administradores.") }
                Button({ onClick { AppSession.logout() } }) { Text("Sair") }
            }
        } else {
            val adminDashboardVm = remember { AdminDashboardViewModel() }
            val adminMetersVm = remember { AdminMetersViewModel() }
            val adminSessionsVm = remember { AdminSessionsViewModel() }
            val adminUsersVm = remember { AdminUsersViewModel() }
            val walletVm = remember { WalletViewModel() }
            
            AdminLayoutWeb(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it },
                onLogout = { currentScreen = "dashboard" }
            ) {
                when (currentScreen) {
                    "dashboard" -> AdminDashboardWeb(adminDashboardVm, { currentScreen = it })
                    "users" -> UsersListWeb(adminUsersVm)
                    "meters" -> MetersListWeb(adminMetersVm)
                    "active_sessions" -> ActiveSessionsWeb(adminSessionsVm)
                    "extract" -> AdminExtractWeb(walletVm) { currentScreen = "dashboard" }
                    "obstructed" -> ObstructedWeb(adminMetersVm, adminSessionsVm)
                    "sessions" -> SessionsListWeb(adminSessionsVm)
                    "logs" -> LogsWeb()
                    else -> AdminDashboardWeb(adminDashboardVm) { currentScreen = it }
                }
            }
        }
    }
}

object AppStylesheet : StyleSheet() {
    val container by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        padding(20.px)
    }

    val card by style {
        marginTop(20.px)
        padding(20.px)
        border(1.px, LineStyle.Solid, Color.lightgray)
    }
}
