package com.ivianuu.fragmentchangehandlers.app.counter

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ivianuu.fragmentchangehandlers.util.d

/**
 * @author Manuel Wrage (IVIanuu)
 */
class CounterLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var count = 0
        set(value) {
            field = value
            d { "init ${toString()}" }
        }

    override fun toString(): String {
        return "Counter layout: $count, ${System.identityHashCode(this)}"
    }

}