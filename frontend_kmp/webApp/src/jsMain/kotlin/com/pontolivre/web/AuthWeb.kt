package com.pontolivre.web

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import com.pontolivre.shared.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginWeb(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            height(100.vh)
            backgroundColor(Color.aliceblue)
            fontFamily("sans-serif")
        }
    }) {
        Div({
            style {
                backgroundColor(Color.white)
                padding(40.px)
                borderRadius(16.px)
                property("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                width(320.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(16.px)
            }
        }) {
            H2({ style { margin(0.px); textAlign("center"); color(Color("#1976d2")) } }) {
                Text("PontoLivre Admin")
            }
            
            P({ style { textAlign("center"); opacity(0.7); margin(0.px) } }) {
                Text("Acesse o painel de controle")
            }

            Div {
                Label(forId = "email") { Text("E-mail") }
                Input(InputType.Email) {
                    id("email")
                    value(email)
                    onInput { email = it.value }
                    style {
                        width(100.percent)
                        padding(10.px)
                        marginTop(4.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, Color.lightgray)
                        property("box-sizing", "border-box")
                    }
                }
            }

            Div {
                Label(forId = "password") { Text("Senha") }
                Input(InputType.Password) {
                    id("password")
                    value(password)
                    onInput { password = it.value }
                    style {
                        width(100.percent)
                        padding(10.px)
                        marginTop(4.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, Color.lightgray)
                        property("box-sizing", "border-box")
                    }
                }
            }

            if (state.error != null) {
                P({ style { color(Color.red); fontSize(14.px); margin(0.px) } }) {
                    Text(state.error!!)
                }
            }

            Button({
                onClick { viewModel.login(email, password, onLoginSuccess) }
                style {
                    padding(12.px)
                    backgroundColor(Color("#1976d2"))
                    color(Color.white)
                    border(0.px)
                    borderRadius(8.px)
                    cursor("pointer")
                    fontWeight("bold")
                }
                if (state.isLoading) disabled()
            }) {
                Text(if (state.isLoading) "Carregando..." else "Entrar")
            }
        }
    }
}
