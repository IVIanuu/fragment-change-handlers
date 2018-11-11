package com.ivianuu.fragmentchangehandlers.lib.internal

import android.view.ViewGroup
import kotlin.reflect.KClass

internal fun <T : Any> newInstanceOrThrow(className: String) = try {
    classForNameOrThrow<T>(className).newInstance() as T
} catch (e: Exception) {
    throw RuntimeException("could not instantiate $className, $e")
}

internal fun <T> classForNameOrThrow(className: String) = try {
    Class.forName(className) as Class<out T>
} catch (e: Exception) {
    throw RuntimeException("couldn't find class $className")
}

internal fun <T : Any> forEachOf(view: ViewGroup, clazz: KClass<T>, action: (T) -> Unit) {
    for (i in 0 until view.childCount) {
        val v = view.getChildAt(i)

        if (clazz.java.isAssignableFrom(v.javaClass)) {
            action(v as T)
        } else if (v is ViewGroup) {
            forEachOf(v, clazz, action)
        }
    }
}