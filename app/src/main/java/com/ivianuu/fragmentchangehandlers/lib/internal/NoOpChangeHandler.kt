package com.ivianuu.fragmentchangehandlers.lib.internal

import android.view.View
import android.view.ViewGroup
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeHandler

internal class NoOpChangeHandler : FragmentChangeHandler() {

    override fun performChange(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onChangeComplete: () -> Unit
    ) {
        onChangeComplete()
    }

    override fun copy() = NoOpChangeHandler()
}