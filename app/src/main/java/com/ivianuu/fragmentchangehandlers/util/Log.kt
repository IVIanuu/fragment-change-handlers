package com.ivianuu.fragmentchangehandlers.util

import android.util.Log

inline fun Any.d(m: () -> String) {
    Log.d("fr: ${javaClass.simpleName}", m())
}