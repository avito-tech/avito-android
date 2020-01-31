package com.avito.android.util

import com.nhaarman.mockito_kotlin.mock
import org.mockito.ArgumentCaptor

inline fun <reified T : Any> capture(instance: T = mock(), function: (InstanceArgumentCaptor<T>) -> Unit): T {
    val instanceArgumentCaptor = InstanceArgumentCaptor(instance)
    function(instanceArgumentCaptor)
    return instanceArgumentCaptor.value
}

inline fun <reified T : Any> argumentCapture(function: (ArgumentCaptor<T>) -> Unit): T {
    val argumentCaptor = ArgumentCaptor.forClass(T::class.java)
    function(argumentCaptor)
    return argumentCaptor.value
}

class InstanceArgumentCaptor<out T : Any>(private val instance: T) {
    val value: T
        get() = argumentCaptor.value
    val allValues: List<T>
        get() = argumentCaptor.allValues

    private val argumentCaptor = ArgumentCaptor.forClass(instance.javaClass)

    fun capture(): T {
        argumentCaptor.capture()
        return instance
    }
}
