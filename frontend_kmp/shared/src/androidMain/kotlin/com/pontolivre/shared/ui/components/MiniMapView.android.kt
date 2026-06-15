package com.pontolivre.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun MiniMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier
) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false) // Desativa gestos para o mini mapa
                
                val pos = GeoPoint(latitude, longitude)
                controller.setZoom(16.0)
                controller.setCenter(pos)
                
                val marker = Marker(this)
                marker.position = pos
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(marker)
                
                // Desativa interação
                setOnTouchListener { _, _ -> true }
            }
        },
        update = { view ->
            val pos = GeoPoint(latitude, longitude)
            view.controller.setCenter(pos)
            view.overlays.clear()
            val marker = Marker(view)
            marker.position = pos
            view.overlays.add(marker)
        },
        modifier = modifier
    )
}
