package com.avito.instrumentation.configuration

import java.io.Serializable

interface Configuration<D : Serializable> {
    fun withData(action: (D) -> Unit)
}
