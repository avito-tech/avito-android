package com.avito.truth

import com.google.common.truth.Truth

inline fun <reified T> assertThat(any: Any, assert: T.() -> Unit) {
    Truth.assertThat(any).isInstanceOf(T::class.java)
    assert(any as T)
}
