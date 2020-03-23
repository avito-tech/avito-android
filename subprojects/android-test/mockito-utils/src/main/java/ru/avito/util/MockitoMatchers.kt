package ru.avito.util

import com.nhaarman.mockitokotlin2.internal.createInstance
import org.hamcrest.Matcher
import org.mockito.Mockito
import org.mockito.hamcrest.MockitoHamcrest
import org.mockito.verification.VerificationMode

inline fun <reified T : Any> any(instance: T) =
    Mockito.any(T::class.java) ?: instance

inline fun <reified T : Any> eq(instance: T) =
    Mockito.eq(instance) ?: instance

inline fun <reified T : Any?> eqOpt(instance: T?) =
    Mockito.eq(instance) ?: instance

inline fun <reified T : Any> isA(instance: T) =
    Mockito.isA(T::class.java) ?: instance

fun <T> same(value: T): T = Mockito.same(value) ?: value

@Suppress("NOTHING_TO_INLINE")
inline fun onlyOnce(): VerificationMode = Mockito.times(1)

@Suppress("NOTHING_TO_INLINE")
inline fun onlyTwice(): VerificationMode = Mockito.times(2)

fun anyLongOrNull(): Long = Mockito.any() ?: 0L

inline fun <reified T : Any> Matcher<T>.asArgumentMatcher(): T =
    MockitoHamcrest.argThat(this) ?: createInstance()
