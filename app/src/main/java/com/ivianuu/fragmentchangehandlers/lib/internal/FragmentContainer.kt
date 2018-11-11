package com.ivianuu.fragmentchangehandlers.lib.internal

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.ivianuu.fragmentchangehandlers.util.d

internal class FragmentContainer(context: Context) : FrameLayout(context) {

    internal var changeRunning = false

    internal var lockedDown = false

    internal var childInterceptor: ChildInterceptor? = null

    override fun onInterceptTouchEvent(ev: MotionEvent) =
        changeRunning || super.onInterceptTouchEvent(ev)

    override fun addView(child: View) {
        d { "add view $child" }

        if (lockedDown) return

        if (childInterceptor != null) {
            childInterceptor?.onAddView(child)
        } else {
            super.addView(child)
        }
    }

    override fun removeView(view: View) {
        d { "remove view $view" }

        if (lockedDown) return

        if (childInterceptor != null) {
            childInterceptor?.onRemoveView(view)
        } else {
            super.removeView(view)
        }
    }

    interface ChildInterceptor {
        fun onAddView(view: View)
        fun onRemoveView(view: View)
    }
}