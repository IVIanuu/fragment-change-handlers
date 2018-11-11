package com.ivianuu.fragmentchangehandlers.app.counter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * @author Manuel Wrage (IVIanuu)
 */
class ChildFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = object : View(requireContext()) {
        init {
            setBackgroundColor(Color.WHITE)
        }

        override fun toString(): String {
            return "Child view ${arguments!!.getInt(KEY_COUNT)}"
        }
    }

    companion object {
        private const val KEY_COUNT = "count"

        fun create(count: Int) = ChildFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_COUNT, count)
            }
        }
    }
}