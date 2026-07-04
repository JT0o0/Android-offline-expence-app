package com.toting.ledger.ui.components

/** Curated swatch palette for category/account colors and theme overrides. */
object Palette {
    val swatches: List<Int> = listOf(
        0xFFE57373, 0xFFF06292, 0xFFBA68C8, 0xFF9575CD,
        0xFF7986CB, 0xFF64B5F6, 0xFF4FC3F7, 0xFF4DD0E1,
        0xFF4DB6AC, 0xFF81C784, 0xFFAED581, 0xFFDCE775,
        0xFFFFD54F, 0xFFFFB74D, 0xFFFF8A65, 0xFFA1887F,
        0xFF90A4AE, 0xFF66BB6A, 0xFF26A69A, 0xFF42A5F5,
    ).map { it.toInt() }
}
