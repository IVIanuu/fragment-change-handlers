package com.ivianuu.fragmentchangehandlers.lib

/*
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


private val initStub: Router.() -> Unit = {}

fun FragmentActivity.attachRouter(
    container: ViewGroup,
    savedInstanceState: Bundle?,
    init: Router.() -> Unit = initStub
) = Router(
    supportFragmentManager,
    container.id,
    savedInstanceState
)
    .apply { setContainer(container) }
    .also { addOnBackPressedCallback(it.onBackPressedCallback) }
    .apply(init)

fun Fragment.attachRouter(
    containerId: Int,
    savedInstanceState: Bundle?,
    init: Router.() -> Unit
) = Router(
    childFragmentManager,
    containerId,
    savedInstanceState
)
    .also { requireActivity().addOnBackPressedCallback(it.onBackPressedCallback) }
    .apply(init)*/