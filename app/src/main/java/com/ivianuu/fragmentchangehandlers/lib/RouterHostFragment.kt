package com.ivianuu.fragmentchangehandlers.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.internal.FragmentContainer

/**
 * @author Manuel Wrage (IVIanuu)
 */
class RouterHostFragment : Fragment() {

    val router get() = _router ?: throw IllegalArgumentException("not available before onCreate")
    private var _router: Router? = null

    private val onBackPressedCallback = OnBackPressedCallback { router.handleBack() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _router = Router(childFragmentManager, id)

        savedInstanceState?.getBundle(KEY_ROUTER_STATE)?.let {
            router.restoreInstanceState(it)
        }
    }

    override fun toString(): String {
        return "RouterHostFragment: $id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentContainer(requireContext())
        .also { it.id = this.id }
        .also { router.container = it }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addOnBackPressedCallback(onBackPressedCallback)
    }

    override fun onDestroyView() {
        router.container = null
        requireActivity().removeOnBackPressedCallback(onBackPressedCallback)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val routerState = Bundle()
        router.saveInstanceState(routerState)
        outState.putBundle(KEY_ROUTER_STATE, routerState)
    }

    companion object {
        private const val KEY_ROUTER_STATE = "RouterHostFragment.routerState"
    }
}