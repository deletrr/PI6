package com.pontolivre.shared.ui.screens.user

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pontolivre.shared.model.ParkingMeterModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun ParkingMapView(
    meters: List<ParkingMeterModel>,
    onMeterSelected: (String) -> Unit
) {
    // A biblioteca Leaflet é para Web. No Android nativo (sem WebView), 
    // a solução padrão para OpenStreetMap é a Osmdroid.
    
    AndroidView(
        factory = { context ->
            // Configuração necessária para evitar bloqueio do servidor OSM
            Configuration.getInstance().userAgentValue = context.packageName
            
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                
                val mapController = controller
                mapController.setZoom(15.0)
                
                // Centralização inicial
                val first = meters.firstOrNull { it.latitude != null }
                val startPoint = if (first != null) {
                    GeoPoint(first.latitude!!, first.longitude!!)
                } else {
                    GeoPoint(-23.5505, -46.6333)
                }
                mapController.setCenter(startPoint)

                // Adiciona Marcadores
                meters.forEach { meter ->
                    if (meter.latitude != null && meter.longitude != null) {
                        val marker = Marker(this)
                        marker.position = GeoPoint(meter.latitude, meter.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = meter.code
                        marker.snippet = meter.description ?: ""
                        
                        marker.setOnMarkerClickListener { m, _ ->
                            onMeterSelected(meter.id)
                            m.showInfoWindow()
                            true
                        }
                        
                        overlays.add(marker)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
