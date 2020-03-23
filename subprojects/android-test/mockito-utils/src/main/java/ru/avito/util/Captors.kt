package ru.avito.util

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor

inline fun <reified T : Any> capture(captureBlock: (KArgumentCaptor<T>) -> Unit): T =
    with(argumentCaptor<T>()) {
        captureBlock(this)
        lastValue
    }

inline fun <reified T : Any> captureAll(captureBlock: (KArgumentCaptor<T>) -> Unit): List<T> =
    with(argumentCaptor<T>()) {
        captureBlock(this)
        allValues
    }
