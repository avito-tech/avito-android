@file:JvmName("AnyExtensionsUtils")

package com.avito.android.util

import org.junit.Assert.assertThat
import ru.avito.util.instanceOf

inline fun <reified T : Any> Any.assertAsInstance(crossinline assertion: T.() -> Unit = {}) {
    assertThat(this, Is(instanceOf<T>()))
    (this as T).assertion()
}
