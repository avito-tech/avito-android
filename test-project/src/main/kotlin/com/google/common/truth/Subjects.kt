package com.google.common.truth

inline fun <reified T : Any> Subject.isInstanceOf() = isInstanceOf(T::class.java)
