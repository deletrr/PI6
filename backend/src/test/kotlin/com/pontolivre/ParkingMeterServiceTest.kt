package com.pontolivre

import com.pontolivre.entity.ParkingStatus
import com.pontolivre.mqtt.MqttSubscriber
import com.pontolivre.repository.ParkingMeterRepository
import com.pontolivre.service.ParkingMeterService
import org.eclipse.paho.client.mqttv3.MqttClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParkingMeterServiceTest {

    @Autowired
    lateinit var meterService: ParkingMeterService

    @Autowired
    lateinit var meterRepository: ParkingMeterRepository

    // Mock out MQTT
    @MockBean lateinit var mqttClient: MqttClient
    @MockBean lateinit var mqttSubscriber: MqttSubscriber

    @Test
    fun `updateStatus deve criar parquimetro orfao se nao existir`() {
        val code = "NEW-PKM-999"
        
        val meter = meterService.updateStatus(code, ParkingStatus.OCCUPIED)
        
        assertNotNull(meter.id)
        assertEquals(code, meter.code)
        assertTrue(meter.orphan)
        assertEquals(ParkingStatus.OCCUPIED, meter.status)
        assertNull(meter.latitude)
        assertNull(meter.longitude)
    }

    @Test
    fun `updateStatus deve ser case-insensitive e encontrar parquimetro existente`() {
        val originalCode = "PKM-ALPHA"
        meterService.updateStatus(originalCode, ParkingStatus.FREE)
        
        // Tenta atualizar usando minusculo
        val updatedMeter = meterService.updateStatus("pkm-alpha", ParkingStatus.OCCUPIED)
        
        assertEquals(originalCode, updatedMeter.code)
        assertEquals(ParkingStatus.OCCUPIED, updatedMeter.status)
        
        // Verifica que nao criou duplicado
        val count = meterRepository.findAll().count { it.code.uppercase() == originalCode }
        assertEquals(1, count)
    }

    @Test
    fun `updateStatus deve normalizar codigo para uppercase`() {
        val inputCode = "pkm-lower-123"
        val meter = meterService.updateStatus(inputCode, ParkingStatus.FREE)
        
        assertEquals("PKM-LOWER-123", meter.code)
        
        val found = meterRepository.findByCode("PKM-LOWER-123")
        assertTrue(found.isPresent)
    }
}
