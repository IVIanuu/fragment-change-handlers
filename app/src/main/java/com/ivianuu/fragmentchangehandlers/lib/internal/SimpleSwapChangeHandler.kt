package com.ivianuu.fragmentchangehandlers.lib.internal

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeHandler

/**
 * A [FragmentChangeHandler] that will instantly swap Views with no animations or transitions.
 */
internal class SimpleSwapChangeHandler : FragmentChangeHandler() {

    private var canceled = false

    private var container: ViewGroup? = null
    private var onChangeComplete: (() -> Unit)? = null

    private val attachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            v.removeOnAttachStateChangeListener(this)

            onChangeComplete?.invoke()
            onChangeComplete = null
            container = null
        }

        override fun onViewDetachedFromWindow(v: View) {
        }
    }

    override fun onAbortPush(newHandler: FragmentChangeHandler, newTop: Fragment?) {
        super.onAbortPush(newHandler, newTop)
        canceled = true
    }

    override fun completeImmediately() {
        onChangeComplete?.let {
            it()
            onChangeComplete = null
            container?.removeOnAttachStateChangeListener(attachStateChangeListener)
            container = null
        }
    }

    override fun performChange(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onChangeComplete: () -> Unit
    ) {
        if (!canceled) {
            if (from != null && (!isPush)) {
                container.removeView(from)
            }

            if (to != null && to.parent == null) {
                container.addView(to)
            }
        }

        if (container.windowToken != null) {
            onChangeComplete()
        } else {
            this.onChangeComplete = onChangeComplete
            this.container = container
            container.addOnAttachStateChangeListener(attachStateChangeListener)
        }
    }

    override fun copy() = SimpleSwapChangeHandler()

}