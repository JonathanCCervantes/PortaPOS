package com.jonathan.portapos.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PortaPOSColorScheme = lightColorScheme(
    primary         = BluePrimary,
    onPrimary       = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueDark,
    secondary       = GreenSuccess,
    onSecondary     = Color.White,
    background      = BackgroundLight,
    surface         = SurfaceCard,
    onBackground    = TextPrimary,
    onSurface       = TextPrimary,
    error           = ErrorRed,
    onError         = Color.White,
)

@Composable
fun PortaPOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PortaPOSColorScheme,
        typography = Typography(),
        content = content
    )
}
