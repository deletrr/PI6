package com.pontolivre.mqtt

import com.pontolivre.config.MqttProperties
import com.pontolivre.entity.MqttLog
import com.pontolivre.entity.ParkingStatus
import com.pontolivre.repository.MqttLogRepository
import com.pontolivre.service.ParkingMeterService
import com.pontolivre.service.ParkingSessionService
import org.eclipse.paho.client.mqttv3.*
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.nio.charset.StandardCharsets

@Component
class MqttSubscriber(
    private val mqttClient: MqttClient,
    private val mqttConnectOptions: MqttConnectOptions,
    private val meterService: ParkingMeterService,
    private val sessionService: ParkingSessionService,
    private val mqttLogRepository: MqttLogRepository,
    private val transactionTemplate: TransactionTemplate,
    private val properties: MqttProperties
) {

    private val log = LoggerFactory.getLogger(MqttSubscriber::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun connect() {
        println(">>>> [MQTT HEARTBEAT] STARTUP INITIATED <<<<")
        println(">>>> Broker: ${properties.brokerUrl}")
        println(">>>> Topics: ${properties.topics}")

        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                val msg = ">>>> [MQTT SUCCESS] CONECTADO AO BROKER: $serverURI"
                println(msg)
                log.info(msg)
                subscribeToTopics()
            }

            override fun connectionLost(cause: Throwable) {
                val msg = ">>>> [MQTT ERROR] CONEXAO PERDIDA: ${cause.message}"
                println(msg)
                log.error(msg)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val payload = String(message.payload, StandardCharsets.UTF_8).trim()
                println(">>>> [MQTT MESSAGE] TOPICO: $topic | PAYLOAD: $payload")
                handleMessage(topic, payload)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })

        Thread {
            while (true) {
                if (!mqttClient.isConnected) {
                    try {
                        println(">>>> [MQTT] Tentando conectar em ${properties.brokerUrl}...")
                        mqttClient.connect(mqttConnectOptions)
                    } catch (e: Exception) {
                        println(">>>> [MQTT] Falha na conexao: ${e.message}")
                    }
                }
                Thread.sleep(10000)
            }
        }.start()
    }

    private fun subscribeToTopics() {
        val topicsToSubscribe = if (properties.topics.isEmpty()) listOf("parquimetro/+/status") else properties.topics
        topicsToSubscribe.forEach { topic ->
            try {
                mqttClient.subscribe(topic, 1)
                println(">>>> [MQTT] Inscrito com sucesso no topico: $topic")
            } catch (e: Exception) {
                println(">>>> [MQTT] Erro ao inscrever no topico $topic: ${e.message}")
            }
        }
    }

    private fun handleMessage(topic: String, payload: String) {
        val meterCode = topic.split("/").getOrNull(1) ?: run {
            log.warn("Tópico inválido (esperado parquimetro/CODIGO/status): $topic")
            return
        }

        println(">>>> [MQTT MESSAGE] INICIANDO PROCESSAMENTO: $meterCode | PAYLOAD: $payload")

        // 1. Persistir o log primeiro em uma transação separada ou garantida
        val mqttLog = transactionTemplate.execute {
            val logEntity = MqttLog(topic = topic, payload = payload, meterCode = meterCode)
            mqttLogRepository.save(logEntity)
        } ?: return

        // 2. Tentar processar o status
        try {
            val statusPayload = payload.lowercase().trim()
            val status = when {
                statusPayload.startsWith("ocupado") -> ParkingStatus.OCCUPIED
                statusPayload == "livre" -> ParkingStatus.FREE
                else -> {
                    println(">>>> [MQTT WARNING] Payload desconhecido ignorado: $payload")
                    null
                }
            }

            if (status != null) {
                transactionTemplate.execute {
                    val meter = meterService.updateStatus(meterCode, status)
                    println(">>>> [MQTT INFO] Parquímetro $meterCode atualizado para $status")

                    if (status == ParkingStatus.OCCUPIED && statusPayload.contains(":")) {
                        val sessionCode = statusPayload.substringAfter(":")
                        if (sessionCode.isNotBlank()) {
                            runCatching {
                                sessionService.createPendingSession(meterCode, sessionCode)
                            }.onFailure {
                                log.error("Erro ao criar sessão pendente via MQTT: ${it.message}")
                            }
                        }
                    }

                    if (status == ParkingStatus.FREE) {
                        runCatching {
                            sessionService.endSessionByMqtt(meter.id)
                        }.onFailure {
                            log.warn("Nenhuma sessão ativa para encerrar: ${it.message}")
                        }
                    }
                    
                    // Atualiza o log como processado
                    val logToUpdate = mqttLogRepository.findById(mqttLog.id).get()
                    logToUpdate.processed = true
                    mqttLogRepository.save(logToUpdate)
                }
                println(">>>> [MQTT SUCCESS] Status atualizado e log marcado como processado.")
            }
        } catch (e: Exception) {
            println(">>>> [MQTT ERROR] Falha ao processar mensagem: ${e.message}")
            log.error("Erro ao processar MQTT ($topic): ${e.message}", e)
        }
    }

    fun publish(topic: String, payload: String) {
        runCatching {
            if (!mqttClient.isConnected) mqttClient.connect(mqttConnectOptions)
            val message = MqttMessage(payload.toByteArray(StandardCharsets.UTF_8))
            message.qos = 1
            mqttClient.publish(topic, message)
            log.info("MQTT publicado [$topic]: $payload")
        }.onFailure {
            log.error("Falha ao publicar MQTT: ${it.message}")
        }
    }
}
