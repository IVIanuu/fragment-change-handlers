package com.ivianuu.fragmentchangehandlers.lib

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.internal.newInstanceOrThrow

/**
 * FragmentChangeHandlers are responsible for swapping the View for one Fragment to the View
 * of another. They can be useful for performing animations and transitions between Fragments. Several
 * default FragmentChangeHandlers are included.
 */
abstract class FragmentChangeHandler {

    /**
     * Responsible for swapping Views from one Fragment to another.
     */
    abstract fun performChange(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onChangeComplete: () -> Unit
    )

    /**
     * Saves any data about this handler to a Bundle in case the application is killed.
     */
    open fun saveToBundle(bundle: Bundle) {
    }

    /**
     * Restores data that was saved in the [saveToBundle] method.
     */
    open fun restoreFromBundle(bundle: Bundle) {
    }

    /**
     * Will be called on change handlers that push a fragment if the fragment being pushed is
     * popped before it has completed.
     */
    open fun onAbortPush(newHandler: FragmentChangeHandler, newTop: Fragment?) {
    }

    /**
     * Will be called on change handlers that push a fragment if the fragment being pushed is
     * needs to be attached immediately, without any animations or transitions.
     */
    open fun completeImmediately() {
    }

    /**
     * Returns a copy of this FragmentChangeHandler. This method is internally used by the library, so
     * ensure it will return an exact copy of your handler if overriding. If not overriding, the handler
     * will be saved and restored from the Bundle format.
     */
    open fun copy() = fromBundle(toBundle())

    internal fun toBundle() = Bundle().apply {
        putString(KEY_CLASS_NAME, this@FragmentChangeHandler.javaClass.name)
        putBundle(KEY_SAVED_STATE, Bundle().also { saveToBundle(it) })
    }

    companion object {
        private const val KEY_CLASS_NAME = "FragmentChangeHandler.className"
        private const val KEY_SAVED_STATE = "FragmentChangeHandler.savedState"

        internal fun fromBundle(bundle: Bundle): FragmentChangeHandler {
            val className = bundle.getString(KEY_CLASS_NAME)!!
            return newInstanceOrThrow<FragmentChangeHandler>(
                className
            ).apply {
                restoreFromBundle(bundle.getBundle(KEY_SAVED_STATE)!!)
            }
        }
    }
}