package com.ivianuu.fragmentchangehandlers.app.sharedelements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.R
import com.ivianuu.fragmentchangehandlers.lib.popChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.pushChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.requireFragmentRouter
import com.ivianuu.fragmentchangehandlers.lib.toRouterTransaction
import kotlinx.android.synthetic.main.fragment_shared_element_one.*

/**
 * @author Manuel Wrage (IVIanuu)
 */
class SharedElementFragmentOne : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shared_element_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shared_element.setOnClickListener {
            requireFragmentRouter().pushFragment(
                SharedElementFragmentTwo()
                    .toRouterTransaction()
                    .pushChangeHandler(TestSharedElementsChangeHandler())
                    .popChangeHandler(TestSharedElementsChangeHandler())
            )
        }
    }

}