package com.ivianuu.fragmentchangehandlers.lib.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.FragmentChangeHandler

/**
 * A base [FragmentChangeHandler] that facilitates using [android.animation.Animator]s to replace Fragment Views
 */
abstract class AnimatorChangeHandler(duration: Long = DEFAULT_ANIMATION_DURATION) : FragmentChangeHandler() {

    var duration =
        DEFAULT_ANIMATION_DURATION
        private set

    private var animator: Animator? = null
    private var onReadyOrAbortedListener: OnReadyOrAbortedListener? = null

    private var canceled = false
    private var needsImmediateCompletion = false
    private var completed = false

    init {
        this.duration = duration
    }

    override fun performChange(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onChangeComplete: () -> Unit
    ) {
        var readyToAnimate = true
        val addingToView = to != null && to.parent == null

        if (addingToView) {
            if (isPush || from == null) {
                container.addView(to!!)
            } else if (to?.parent == null) {
                container.addView(to, container.indexOfChild(from))
            }

            if (to!!.width <= 0 && to.height <= 0) {
                readyToAnimate = false
                onReadyOrAbortedListener =
                        OnReadyOrAbortedListener(to) {
                            performAnimation(
                                container,
                                from,
                                to,
                                isPush,
                                true,
                                onChangeComplete
                            )
                        }
            }
        }

        if (readyToAnimate) {
            performAnimation(container, from, to, isPush, addingToView, onChangeComplete)
        }
    }

    override fun saveToBundle(bundle: Bundle) {
        super.saveToBundle(bundle)
        bundle.putLong(KEY_DURATION, duration)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        duration = bundle.getLong(KEY_DURATION)
    }

    override fun onAbortPush(newHandler: FragmentChangeHandler, newTop: Fragment?) {
        super.onAbortPush(newHandler, newTop)
        canceled = true

        if (animator != null) {
            animator?.cancel()
        } else if (onReadyOrAbortedListener != null) {
            onReadyOrAbortedListener?.onReadyOrAborted()
        }
    }

    override fun completeImmediately() {
        super.completeImmediately()
        needsImmediateCompletion = true
        if (animator != null) {
            animator?.end()
        } else if (onReadyOrAbortedListener != null) {
            onReadyOrAbortedListener?.onReadyOrAborted()
        }
    }

    /**
     * Should be overridden to return the Animator to use while replacing Views.
     */
    protected abstract fun getAnimator(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean
    ): Animator

    /**
     * Will be called after the animation is complete to reset the View that was removed to its pre-animation state.
     */
    protected abstract fun resetFromView(from: View)

    private fun performAnimation(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean,
        onChangeComplete: () -> Unit
    ) {
        if (canceled) {
            complete(onChangeComplete, null)
            return
        }

        if (needsImmediateCompletion) {
            if (from != null) {
                container.removeView(from)
            }
            complete(onChangeComplete, null)
            if (isPush && from != null) {
                resetFromView(from)
            }
            return
        }

        animator = getAnimator(container, from, to, isPush, toAddedToContainer).apply {
            if (this@AnimatorChangeHandler.duration > 0) {
                duration = this@AnimatorChangeHandler.duration
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    if (from != null) {
                        resetFromView(from)
                    }

                    if (to != null && to.parent == container) {
                        container.removeView(to)
                    }

                    complete(onChangeComplete, this)
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!canceled && animator != null) {
                        if (from != null) {
                            container.removeView(from)
                        }

                        complete(onChangeComplete, this)

                        if (isPush && from != null) {
                            resetFromView(from)
                        }
                    }
                }
            })

            start()
        }
    }

    private fun complete(
        onChangeComplete: () -> Unit,
        animatorListener: Animator.AnimatorListener?
    ) {
        if (!completed) {
            completed = true
            onChangeComplete()
        }

        animator?.let { animator ->
            animatorListener?.let { animator.removeListener(it) }
            animator.cancel()
        }

        animator = null
        onReadyOrAbortedListener = null
    }

    companion object {
        private const val KEY_DURATION = "AnimatorChangeHandler.duration"
        const val DEFAULT_ANIMATION_DURATION = -1L
    }
}
