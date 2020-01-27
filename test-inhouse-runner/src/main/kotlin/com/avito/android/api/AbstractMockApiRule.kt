package com.avito.android.api

import com.avito.android.rule.SimpleRule

abstract class AbstractMockApiRule<T : RequestRegistry> : SimpleRule() {

    private var registry: T? = null

    override fun after() {
        with(requireNotNull(registry)) {
            try {
                registeredMocks.values.forEach(ApiRequest::verify)
            } finally {
                reset()
                registry = null
            }
        }
    }

    protected abstract fun createRegistry(): T

    fun stub(init: T.() -> Unit) {
        if (registry == null) {
            registry = createRegistry()
        }
        registry!!.init()
    }
}
