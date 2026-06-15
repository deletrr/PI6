package com.pontolivre.shared.ui.screens.user

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import com.pontolivre.shared.model.ParkingMeterModel
import kotlinx.browser.document
import kotlinx.browser.window

@JsModule("leaflet")
@JsNonModule
external object L {
    fun map(id: String, options: dynamic = definedExternally): dynamic
    fun tileLayer(url: String, options: dynamic = definedExternally): dynamic
    fun marker(latlng: Array<Double>, options: dynamic = definedExternally): dynamic
}

@Composable
actual fun ParkingMapView(
    meters: List<ParkingMeterModel>,
    onMeterSelected: (String) -> Unit
) {
    val mapId = remember { "map-${window.asDynamic().Math.random()}" }
    
    Div({
        id(mapId)
        style {
            width(100.percent)
            height(100.percent)
            property("min-height", "400.px")
        }
    })

    LaunchedEffect(meters) {
        // Aguarda o elemento ser renderizado no DOM
        val element = document.getElementById(mapId)
        if (element != null) {
            val leaflet = window.asDynamic().L
            if (leaflet != null) {
                // Centraliza no primeiro marcador ou em SP
                val first = meters.firstOrNull { it.latitude != null }
                val initialLat = first?.latitude ?: -23.5505
                val initialLng = first?.longitude ?: -46.6333
                
                val map = leaflet.map(mapId).setView(arrayOf(initialLat, initialLng), 15)
                
                leaflet.tileLayer("https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png", js("{ attribution: '&copy; OpenStreetMap &copy; CARTO' }")).addTo(map)
                
                meters.forEach { meter ->
                    if (meter.latitude != null && meter.longitude != null) {
                        val marker = leaflet.marker(arrayOf(meter.latitude, meter.longitude)).addTo(map)
                        marker.bindPopup("<b>${meter.code}</b><br>${meter.description ?: ""}")
                        marker.on("click", { onMeterSelected(meter.id) })
                    }
                }
            }
        }
    }
}
