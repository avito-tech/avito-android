package com.avito.android.util

public inline fun <reified T> Any.getFieldValue(field: String): T = javaClass.getDeclaredField(field)
    .apply { isAccessible = true }
    .let { it.get(this@getFieldValue) as T }

public fun Any.executeMethod(method: String, vararg arguments: Any?) {
    javaClass.methods.find { it.name == method }!!
        .apply { isAccessible = true }
        .apply {
            invoke(
                this@executeMethod,
                *arguments
            )
        }
}

internal fun Class<*>.isLambda(): Boolean = Function::class.java.isAssignableFrom(this)
