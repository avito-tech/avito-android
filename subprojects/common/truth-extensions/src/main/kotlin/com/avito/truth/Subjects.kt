package com.avito.truth

import com.google.common.truth.Subject

inline fun <reified T : Any> Subject.isInstanceOf() = isInstanceOf(T::class.java)

inline fun <reified T : Any> Subject.isNotInstanceOf() = isNotInstanceOf(T::class.java)
