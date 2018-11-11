package com.ivianuu.fragmentchangehandlers.lib

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

fun FragmentActivity.findRouter(id: Int) = supportFragmentManager.findRouter(id)

fun Fragment.findRouter(id: Int) = childFragmentManager.findRouter(id)

fun FragmentManager.findRouter(id: Int) =
    (findFragmentById(id) as? RouterHostFragment)?.router
        ?: throw IllegalArgumentException("no router found with id $id")