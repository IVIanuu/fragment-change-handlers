package com.ivianuu.fragmentchangehandlers.lib.common

import android.os.Build
import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeHandler

/**
 * A base [FragmentChangeHandler] that facilitates using [android.transition.Transition]s to replace Fragment Views.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
abstract class TransitionChangeHandler : FragmentChangeHandler() {

    private var canceled = false
    private var needsImmediateCompletion = false

    override fun performChange(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onChangeComplete: () -> Unit
    ) {
        if (canceled) {
            onChangeComplete()
            return
        }

        if (needsImmediateCompletion) {
            executePropertyChanges(container, from, to, null, isPush)
            onChangeComplete()
            return
        }

        val transition = getTransition(container, from, to, isPush)
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
                onChangeComplete()
            }

            override fun onTransitionEnd(transition: Transition) {
                onChangeComplete()
            }
        })

        prepareForTransition(
            container,
            from,
            to,
            transition,
            isPush
        ) {
            if (!canceled) {
                TransitionManager.beginDelayedTransition(container, transition)
                executePropertyChanges(container, from, to, transition, isPush)
            }
        }
    }

    override fun onAbortPush(newHandler: FragmentChangeHandler, newTop: Fragment?) {
        super.onAbortPush(newHandler, newTop)
        canceled = true
    }

    override fun completeImmediately() {
        super.completeImmediately()
        needsImmediateCompletion = true
    }

    /**
     * Should be overridden to return the Transition to use while replacing Views.
     */
    protected abstract fun getTransition(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean
    ): Transition

    /**
     * Called before a transition occurs. This can be used to reorder views, set their transition names, etc. The transition will begin
     * when `onTransitionPreparedListener` is called.
     */
    protected open fun prepareForTransition(
        container: ViewGroup,
        from: View?,
        to: View?,
        transition: Transition,
        isPush: Boolean,
        onTransitionPrepared: () -> Unit
    ) {
        onTransitionPrepared()
    }

    /**
     * This should set all view properties needed for the transition to work properly. By default it removes the "from" view
     * and adds the "to" view.
     */
    protected open fun executePropertyChanges(
        container: ViewGroup,
        from: View?,
        to: View?,
        transition: Transition?,
        isPush: Boolean
    ) {
        if (from != null && from.parent == container) {
            container.removeView(from)
        }
        if (to != null && to.parent == null) {
            container.addView(to)
        }
    }
}
