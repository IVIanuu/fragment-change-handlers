package com.ivianuu.fragmentchangehandlers.app.sharedelements

import android.annotation.TargetApi
import android.os.Build
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import com.ivianuu.fragmentchangehandlers.lib.common.SharedElementTransitionChangeHandler

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class TestSharedElementsChangeHandler : SharedElementTransitionChangeHandler() {

    override fun getSharedElementTransition(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean
    ): Transition? {
        return TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeClipBounds())
            .addTransition(ChangeTransform())
    }

    override fun configureSharedElements(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean
    ) {
        addSharedElement("shared_element")
        waitOnSharedElementNamed("shared_element")
    }

}