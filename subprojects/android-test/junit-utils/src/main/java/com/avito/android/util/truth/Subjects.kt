package com.avito.android.util.truth

import com.google.common.truth.Subject

inline fun <reified T> Subject.isInstanceOf() = isInstanceOf(T::class.java)
