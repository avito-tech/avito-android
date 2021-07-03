package com.avito.truth

import com.google.common.truth.Truth.assertThat

public inline fun <reified T : Any> assertThat(any: Any?, assert: T.() -> Unit) {
    assertThat(any).isNotNull()
    assertThat(any).isInstanceOf<T>()
    assert(any as T)
}
