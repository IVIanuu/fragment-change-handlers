package com.ivianuu.fragmentchangehandlers.app.counter

import android.graphics.Color

/**
 * @author Manuel Wrage (IVIanuu)
 */
object ColorGenerator {

    private val COLORS = arrayOf(
        Color.BLUE, Color.RED, Color.GREEN,
        Color.MAGENTA, Color.CYAN, Color.YELLOW
    )

    private var lastColor = -1

    fun generate() = COLORS
        .filter { it != lastColor }
        .shuffled()
        .first()
        .also { lastColor = it }

}