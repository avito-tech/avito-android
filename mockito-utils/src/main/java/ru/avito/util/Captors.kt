package ru.avito.util

import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor

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
