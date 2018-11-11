package com.ivianuu.fragmentchangehandlers.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ivianuu.fragmentchangehandlers.R
import com.ivianuu.fragmentchangehandlers.app.counter.CounterFragment
import com.ivianuu.fragmentchangehandlers.app.sharedelements.SharedElementFragmentOne
import com.ivianuu.fragmentchangehandlers.lib.findRouter
import com.ivianuu.fragmentchangehandlers.lib.tag
import com.ivianuu.fragmentchangehandlers.lib.toRouterTransaction
import com.ivianuu.fragmentchangehandlers.util.d

class MainActivity : AppCompatActivity() {

    private val router by lazy { findRouter(R.id.root_router) }

    private val sharedElements = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!router.hasRootFragment) {
            router.pushFragment(
                if (sharedElements) {
                    SharedElementFragmentOne()
                        .toRouterTransaction()
                } else {
                    CounterFragment.create(1)
                        .toRouterTransaction()
                        .tag("1")
                }
            )
        }
    }

}