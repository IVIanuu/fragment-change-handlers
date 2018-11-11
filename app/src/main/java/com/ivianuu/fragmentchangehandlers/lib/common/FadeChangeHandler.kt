package com.ivianuu.fragmentchangehandlers.lib.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup

/**
 * An [AnimatorChangeHandler] that will cross fade two views
 */
open class FadeChangeHandler(duration: Long = DEFAULT_ANIMATION_DURATION) : AnimatorChangeHandler(duration) {

    override fun getAnimator(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean
    ): Animator {
        val animator = AnimatorSet()

        if (to != null) {
            val start = (if (toAddedToContainer) 0f else to.alpha).toFloat()
            animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, start, 1f))
        }

        if (from != null) {
            animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0f))
        }

        return animator
    }

    override fun resetFromView(from: View) {
        from.alpha = 1f
    }

    override fun copy() =
        FadeChangeHandler(duration)
}