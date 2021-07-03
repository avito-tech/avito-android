package com.avito.truth

import com.google.common.truth.Subject

public inline fun <reified T : Any> Subject.isInstanceOf(): Unit = isInstanceOf(T::class.java)

public inline fun <reified T : Any> Subject.isNotInstanceOf(): Unit = isNotInstanceOf(T::class.java)
