package com.pontolivre.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "mqtt")
data class MqttProperties(
    var brokerUrl: String = "tcp://localhost:1883",
    var clientId: String = "pontolivre-backend",
    var username: String = "",
    var password: String = "",
    var topics: List<String> = listOf("parquimetro/+/status")
)
