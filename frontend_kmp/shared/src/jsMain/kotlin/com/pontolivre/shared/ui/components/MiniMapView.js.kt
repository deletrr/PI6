package com.pontolivre.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
actual fun MiniMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier
) {
    Div {
        Text("Mapa [${latitude}, ${longitude}]")
    }
}
