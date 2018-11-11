package com.ivianuu.fragmentchangehandlers.lib

import androidx.fragment.app.Fragment

fun Fragment.toRouterTransaction() = RouterTransaction(this)

val Fragment.fragmentRouter get() = (parentFragment as? RouterHostFragment)?.router

fun Fragment.requireFragmentRouter() = fragmentRouter
    ?: throw IllegalArgumentException("not attached to RouterHostFragment")