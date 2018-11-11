package com.ivianuu.fragmentchangehandlers.lib.common

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.internal.newInstanceOrThrow

/**
 * A base [FragmentChangeHandler] that facilitates using [android.transition.Transition]s to replace Fragment Views.
 * If the target device is running on a version of Android that doesn't support transitions, a fallback [FragmentChangeHandler] will be used.
 */
open class TransitionChangeHandlerCompat : FragmentChangeHandler {

    private lateinit var changeHandler: FragmentChangeHandler

    constructor()

    /**
     * Constructor that takes a [TransitionChangeHandler] for use with compatible devices, as well as a fallback
     * [FragmentChangeHandler] for use with older devices.
     */
    constructor(
        transitionChangeHandler: TransitionChangeHandler,
        fallbackChangeHandler: FragmentChangeHandler
    ) {
        changeHandler = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transitionChangeHandler
        } else {
            fallbackChangeHandler
        }
    }

    override fun performChange(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onChangeComplete: () -> Unit
    ) {
        changeHandler.performChange(container, from, to, isPush, onChangeComplete)
    }

    override fun saveToBundle(bundle: Bundle) {
        super.saveToBundle(bundle)
        bundle.putString(KEY_CHANGE_HANDLER_CLASS, changeHandler.javaClass.name)

        val stateBundle = Bundle()
        changeHandler.saveToBundle(stateBundle)
        bundle.putBundle(KEY_HANDLER_STATE, stateBundle)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        val className = bundle.getString(KEY_CHANGE_HANDLER_CLASS)!!
        changeHandler = newInstanceOrThrow(className)
        changeHandler.restoreFromBundle(bundle.getBundle(KEY_HANDLER_STATE)!!)
    }

    override fun copy() = TransitionChangeHandlerCompat()
        .also { it.changeHandler = changeHandler.copy() }

    override fun onAbortPush(newHandler: FragmentChangeHandler, newTop: Fragment?) {
        changeHandler.onAbortPush(newHandler, newTop)
    }

    override fun completeImmediately() {
        changeHandler.completeImmediately()
    }

    companion object {
        private const val KEY_CHANGE_HANDLER_CLASS = "TransitionChangeHandlerCompat.changeHandler.class"
        private const val KEY_HANDLER_STATE = "TransitionChangeHandlerCompat.changeHandler.state"
    }
}