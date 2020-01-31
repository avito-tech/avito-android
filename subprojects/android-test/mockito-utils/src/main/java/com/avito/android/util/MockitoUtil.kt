@file:JvmName("MockitoUtil")

package com.avito.android.util

import com.nhaarman.mockito_kotlin.createinstance.createInstance
import com.nhaarman.mockito_kotlin.mock
import org.mockito.Mockito.any
import org.mockito.internal.stubbing.defaultanswers.ForwardsInvocations
import org.mockito.stubbing.Answer
import kotlin.jvm.internal.Reflection.createKotlinClass

/** For Java **/
fun <T> anyOrNull(clazz: Class<T>): T = any() ?: createInstance(clazz)

@Suppress("UNCHECKED_CAST")
private fun <T> createInstance(clazz: Class<T>): T {
    val kClass = createKotlinClass(clazz)
    return createInstance(kClass) as T
}

inline fun <reified T : Any> mockDefault(value: Any): T {
    return mock(defaultAnswer = Answer { value })
}

inline fun <reified T : Any> mockDelegating(delegatedObject: Any): T {
    return mock(defaultAnswer = ForwardsInvocations(delegatedObject))
}
