package com.ivianuu.fragmentchangehandlers.lib

import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * A listener interface useful for allowing external classes to be notified of change events.
 */
interface FragmentChangeListener {
    /**
     * Called when a [FragmentChangeHandler] has started changing [Fragment]s
     */
    fun onChangeStarted(
        to: Fragment?,
        from: Fragment?,
        isPush: Boolean,
        container: ViewGroup,
        handler: FragmentChangeHandler
    ) {
    }

    /**
     * Called when a [FragmentChangeHandler] has completed changing [Fragment]s
     */
    fun onChangeCompleted(
        to: Fragment?,
        from: Fragment?,
        isPush: Boolean,
        container: ViewGroup,
        handler: FragmentChangeHandler
    ) {
    }
}