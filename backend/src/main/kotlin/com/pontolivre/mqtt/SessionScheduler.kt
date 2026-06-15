package com.pontolivre.mqtt

import com.pontolivre.service.ParkingSessionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SessionScheduler(
    private val sessionService: ParkingSessionService
) {
    private val log = LoggerFactory.getLogger(SessionScheduler::class.java)

    // RN06 — verifica sessões que excederam 2h a cada 5 minutos
    @Scheduled(fixedDelay = 300_000)
    fun enforceMaxSessionTime() {
        log.debug("Verificando sessões com tempo excedido...")
        runCatching {
            sessionService.enforceMaxSessionTime()
        }.onFailure {
            log.error("Erro ao encerrar sessões com overtime: ${it.message}")
        }
    }
}
