package com.pontolivre.config

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class MqttConfig(private val properties: MqttProperties) {

    private val log = org.slf4j.LoggerFactory.getLogger(MqttConfig::class.java)

    @Bean
    fun mqttConnectOptions(): MqttConnectOptions {
        log.info("Configurando MqttConnectOptions para: ${properties.brokerUrl}")
        return MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 60
            if (properties.username.isNotBlank()) {
                userName = properties.username
                this.password = properties.password.toCharArray()
            }
        }
    }

    @Bean
    fun mqttClient(): MqttClient {
        val broker = if (properties.brokerUrl.isNullOrBlank()) "tcp://localhost:1883" else properties.brokerUrl
        val clientId = "backend-${System.currentTimeMillis()}"
        println(">>>> [DIAGNOSTICO] CRIANDO MQTT CLIENT: Broker=$broker, ClientID=$clientId")
        return MqttClient(broker, clientId, MemoryPersistence())
    }


    @Bean
    @Profile("test")
    fun mqttClientMock(): MqttClient {
        println(">>>> [DIAGNOSTICO] MODO TESTE ATIVO - USANDO MOCK MQTT")
        return MqttClient("tcp://localhost:1883", "test-client-${System.nanoTime()}", MemoryPersistence())
    }
}
