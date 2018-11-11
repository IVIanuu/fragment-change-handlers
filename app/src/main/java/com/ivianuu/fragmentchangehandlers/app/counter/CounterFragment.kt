package com.ivianuu.fragmentchangehandlers.app.counter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.R
import com.ivianuu.fragmentchangehandlers.lib.RouterHostFragment
import com.ivianuu.fragmentchangehandlers.lib.common.FadeChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.common.HorizontalChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.common.VerticalChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.findRouter
import com.ivianuu.fragmentchangehandlers.lib.popChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.pushChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.requireFragmentRouter
import com.ivianuu.fragmentchangehandlers.lib.tag
import com.ivianuu.fragmentchangehandlers.lib.toRouterTransaction
import kotlinx.android.synthetic.main.fragment_counter.*

/**
 * @author Manuel Wrage (IVIanuu)
 */
class CounterFragment : Fragment() {

    private val countInt by lazy { arguments!!.getInt(KEY_COUNT) }

    //private val childFragmentRouter by lazy { findRouter(Int.MAX_VALUE - countInt) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(Int.MAX_VALUE - countInt, RouterHostFragment())
                .commitNow()
        }*/

        /**
         *   if (!childFragmentRouter.hasRootFragment) {
        childFragmentRouter.setRoot(
        ChildFragment.create(countInt).toRouterTransaction()
        .pushChangeHandler(FadeChangeHandler(2000))
        .popChangeHandler(FadeChangeHandler(2000))
        )

        (0 until 5)
        .map {
        ChildFragment.create(countInt).toRouterTransaction()
        .pushChangeHandler(FadeChangeHandler(2000))
        .popChangeHandler(FadeChangeHandler(2000))
        }
        .forEach { childFragmentRouter.pushFragment(it) }
        }
         */
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_counter, container, false)
        .also { (it as CounterLayout).count = countInt }
        .also {
            it.findViewById<FrameLayout>(R.id.child_router_container).apply {
                addView(FrameLayout(requireContext()).apply {
                    id = Int.MAX_VALUE - countInt
                })
            }
        }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        count.text = "Count: $countInt"

        counter_layout.setBackgroundColor(
            arguments!!.getInt(KEY_COLOR)
        )

        up.setOnClickListener {
            requireFragmentRouter().pushFragment(
                CounterFragment.create(countInt + 1)
                    .toRouterTransaction()
                    .tag("${countInt + 1}")
                    .pushChangeHandler(VerticalChangeHandler())
                    .popChangeHandler(VerticalChangeHandler())
            )
        }

        down.setOnClickListener { requireFragmentRouter().handleBack() }

        replace_up.setOnClickListener {
            requireFragmentRouter().replaceTopFragment(
                CounterFragment.create(countInt + 1)
                    .toRouterTransaction()
                    .tag("${countInt + 1}")
                    .pushChangeHandler(VerticalChangeHandler())
                    .popChangeHandler(HorizontalChangeHandler())
            )
        }

        pop_to_root.setOnClickListener {
            requireFragmentRouter().popToRoot(FadeChangeHandler())
        }

        pop_to_five.setOnClickListener {
            requireFragmentRouter().popToTag("5", VerticalChangeHandler())
        }

        ten_up.setOnClickListener {
            val backstack = requireFragmentRouter()
                .backstack.toMutableList()

            (countInt + 1 until countInt + 1 + 10)
                .map {
                    CounterFragment.create(it)
                        .toRouterTransaction()
                        .tag("$it")
                        .pushChangeHandler(VerticalChangeHandler())
                        .popChangeHandler(HorizontalChangeHandler())
                }
                .forEach { backstack.add(it) }

            requireFragmentRouter().setBackstack(backstack, HorizontalChangeHandler())
        }

        five_down.setOnClickListener {
            val fragment = requireFragmentRouter().findFragmentByTag("${countInt - 5}")

            if (fragment != null) {
                requireFragmentRouter().popToFragment(fragment)
            } else {
                requireFragmentRouter().popToRoot()
            }
        }

        set_three.setOnClickListener {
            requireFragmentRouter().setBackstack(
                (1..3)
                    .map {
                        CounterFragment.create(it)
                            .toRouterTransaction()
                            .tag(it.toString())
                            .pushChangeHandler(VerticalChangeHandler())
                            .popChangeHandler(HorizontalChangeHandler())
                    },
                FadeChangeHandler()
            )
        }

        shuffle.setOnClickListener {
            requireFragmentRouter().setBackstack(requireFragmentRouter().backstack.shuffled(), FadeChangeHandler(100))
        }

        remove_second.setOnClickListener {
            requireFragmentRouter().findFragmentByTag("2")?.let { requireFragmentRouter().popFragment(it) }
        }
    }

    override fun toString(): String {
        return "Count: ${arguments!!.getInt(KEY_COUNT)}"
    }

    companion object {
        private const val KEY_COLOR = "color"
        private const val KEY_COUNT = "count"
        fun create(count: Int) = CounterFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_COLOR, ColorGenerator.generate())
                putInt(KEY_COUNT, count)
            }
        }
    }
}