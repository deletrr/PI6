package com.pontolivre

import com.fasterxml.jackson.databind.ObjectMapper
import com.pontolivre.dto.LoginRequest
import com.pontolivre.dto.RegisterRequest
import com.pontolivre.mqtt.MqttSubscriber
import org.eclipse.paho.client.mqttv3.MqttClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    // Mock out MQTT to prevent real connection attempts during tests
    @MockBean lateinit var mqttClient: MqttClient
    @MockBean lateinit var mqttSubscriber: MqttSubscriber

    @Test
    fun `deve registrar novo usuario e retornar token`() {
        val request = RegisterRequest(
            name = "Teste Silva",
            email = "teste.register@email.com",
            password = "Senha@123",
            cpf = "98765432100",
            phone = "(11) 91234-5678"
        )
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.token") { isNotEmpty() }
            jsonPath("$.user.email") { value("teste.register@email.com") }
            jsonPath("$.user.role") { value("USER") }
        }
    }

    @Test
    fun `deve rejeitar registro com email duplicado`() {
        // First registration
        val first = RegisterRequest(
            name = "Duplicado Um", email = "dup@email.com",
            password = "Senha@123", cpf = "11122233344"
        )
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(first)
        }
        // Second with same email
        val second = first.copy(name = "Duplicado Dois", cpf = "55566677788")
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(second)
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `deve fazer login com credenciais validas`() {
        // Register first
        val reg = RegisterRequest(
            name = "Login Test", email = "logintest@email.com",
            password = "Senha@123", cpf = "99988877766"
        )
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(reg)
        }
        // Login
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest("logintest@email.com", "Senha@123"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { isNotEmpty() }
            jsonPath("$.user.role") { value("USER") }
        }
    }

    @Test
    fun `deve rejeitar login com senha incorreta`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest("naoexiste@email.com", "errada"))
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `deve rejeitar registro sem email`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Teste","password":"Senha@123","cpf":"12312312300"}"""
        }.andExpect { status { isBadRequest() } }
    }
}
