package com.ivianuu.fragmentchangehandlers.lib.internal

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.who
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeListener
import com.ivianuu.fragmentchangehandlers.util.d

/**
 * @author Manuel Wrage (IVIanuu)
 */
internal class ChangeManager {

    private val inProgressChangeHandlers = mutableMapOf<String, ChangeHandlerData>()

    fun executeChange(transaction: ChangeTransaction) {
        d { "execute change $transaction" }

        val (to, from, isPush, container, inHandler, listeners) = transaction

        val handler = inHandler?.copy() ?: SimpleSwapChangeHandler()

        if (from != null) {
            if (isPush) {
                completeChangeImmediately(from.fragment.who)
            } else {
                abortOrCompleteChange(from.fragment, to?.fragment, handler)
            }
        }

        if (to != null) {
            inProgressChangeHandlers[to.fragment.who] = ChangeHandlerData(handler, isPush)
        }

        container.changeRunning = true

        listeners.forEach { it.onChangeStarted(to?.fragment,
            from?.fragment, isPush, container, handler) }

        val toView = to?.view

        if (toView != null) {
            // todo to.changeStarted(handler, toChangeType)
        }

        val fromView = from?.view

        if (fromView != null) {
            // todo ? from.changeStarted(handler, fromChangeType)
        }

        handler.performChange(
            container,
            fromView,
            toView,
            isPush
        ) {
            // todo ? from?.changeEnded(handler, fromChangeType)

            if (to != null) {
                inProgressChangeHandlers.remove(to.fragment.who)
                // todo ? to.changeEnded(handler, toChangeType)
            }

            container.changeRunning = false

            listeners.forEach {
                it.onChangeCompleted(to?.fragment, from?.fragment, isPush, container, handler)
            }

            // make sure that we remove the view
            (fromView?.parent as? FragmentContainer)?.removeView(fromView)
        }
    }

    fun completeChangeImmediately(fragmentInstanceId: String): Boolean {
        val changeHandlerData = inProgressChangeHandlers[fragmentInstanceId]
        if (changeHandlerData != null) {
            changeHandlerData.changeHandler.completeImmediately()
            inProgressChangeHandlers.remove(fragmentInstanceId)
            return true
        }

        return false
    }

    fun abortOrCompleteChange(
        toAbort: Fragment,
        newFragment: Fragment?,
        newChangeHandler: FragmentChangeHandler
    ) {
        val changeHandlerData = inProgressChangeHandlers[toAbort.who]
        if (changeHandlerData != null) {
            if (changeHandlerData.isPush) {
                changeHandlerData.changeHandler.onAbortPush(newChangeHandler, newFragment)
            } else {
                changeHandlerData.changeHandler.completeImmediately()
            }

            inProgressChangeHandlers.remove(toAbort.who)
        }
    }

}

internal data class FragmentWithView(
    val fragment: Fragment,
    var view: View?
)

internal data class ChangeTransaction(
    val to: FragmentWithView?,
    val from: FragmentWithView?,
    val isPush: Boolean,
    val container: FragmentContainer,
    val changeHandler: FragmentChangeHandler?,
    val listeners: List<FragmentChangeListener>
)

internal data class ChangeHandlerData(
    val changeHandler: FragmentChangeHandler,
    val isPush: Boolean
)