package com.toting.ledger.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Returns a legible foreground color (near-black or white) for content on [background]. */
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
