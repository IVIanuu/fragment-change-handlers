package com.ivianuu.fragmentchangehandlers.lib

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activeFragments
import androidx.fragment.app.who

class RouterTransaction(val fragment: Fragment) {

    /**
     * The tag of this transaction
     */
    var tag: String? = null
        set(value) {
            checkModify()
            field = value
        }

    /**
     * The push change handler of this transaction
     */
    var pushChangeHandler: FragmentChangeHandler? = null
        set(value) {
            checkModify()
            field = value
        }

    /**
     * The pop change handler of this transaction
     */
    var popChangeHandler: FragmentChangeHandler? = null
        set(value) {
            checkModify()
            field = value
        }

    internal var attachedToRouter = false

    internal fun saveInstanceState() = Bundle().apply {
        putString(KEY_WHO, fragment.who)
        putString(KEY_TAG, tag)
        pushChangeHandler?.let { putBundle(KEY_PUSH_CHANGE_HANDLER, it.toBundle()) }
        popChangeHandler?.let { putBundle(KEY_POP_CHANGE_HANDLER, it.toBundle()) }
        putBoolean(KEY_ATTACHED_TO_ROUTER, attachedToRouter)
    }

    override fun toString(): String {
        return fragment.toString()
    }

    private fun checkModify() {
        if (attachedToRouter) {
            throw IllegalStateException("transactions cannot be modified after being added to a Router.")
        }
    }

    companion object {
        private const val KEY_WHO = "RouterTransaction.who"
        private const val KEY_TAG = "RouterTransaction.tag"
        private const val KEY_PUSH_CHANGE_HANDLER = "RouterTransaction.pushChangeHandler"
        private const val KEY_POP_CHANGE_HANDLER = "RouterTransaction.popChangeHandler"
        private const val KEY_ATTACHED_TO_ROUTER = "RouterTransaction.attachedToRouter"

        fun fromBundle(bundle: Bundle, fm: FragmentManager): RouterTransaction {
            val who = bundle.getString(KEY_WHO)
            val tag = bundle.getString(KEY_TAG)

            val fragment = fm.activeFragments.firstOrNull { it.who == who }
                ?: throw IllegalStateException("failed to get fragment $who, tag $tag")
            val pushChangeHandler =bundle.getBundle(KEY_PUSH_CHANGE_HANDLER)
                ?.let { FragmentChangeHandler.fromBundle(it) }
            val popChangeHandler = bundle.getBundle(KEY_POP_CHANGE_HANDLER)
                ?.let { FragmentChangeHandler.fromBundle(it) }

            return RouterTransaction(fragment).apply {
                this.tag = tag
                this.pushChangeHandler = pushChangeHandler
                this.popChangeHandler = popChangeHandler
                this.attachedToRouter = bundle.getBoolean(KEY_ATTACHED_TO_ROUTER)
            }
        }
    }
}

fun RouterTransaction.tag(tag: String?) = apply { this.tag = tag }

fun RouterTransaction.pushChangeHandler(handler: FragmentChangeHandler?) = apply {
    this.pushChangeHandler = handler
}

fun RouterTransaction.popChangeHandler(handler: FragmentChangeHandler?) = apply {
    this.popChangeHandler = handler
}