package com.avito.android.api

import com.avito.android.rule.SimpleRule
import kotlinx.coroutines.runBlocking

abstract class AbstractMockApiRule<T : RequestRegistry> : SimpleRule() {

    private var registry: T? = null

    override fun after() {
        val registry = registry ?: return // can be null if another rule failed after us
        try {
            registry.registeredMocks.values.forEach(ApiRequest::verify)
        } finally {
            registry.reset()
            this.registry = null
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

abstract class AbstractMockApiRuleCrt<T : RequestRegistry> : SimpleRule() {

    private var registry: T? = null

    override fun after() {
        val registry = registry ?: return // can be null if another rule failed after us
        try {
            registry.registeredMocks.values.forEach(ApiRequest::verify)

            runBlocking {
                registry.registeredMocksCrt.values.forEach { it.verify() }
            }
        } finally {
            registry.reset()
            this.registry = null
        }
    }

    protected abstract fun createRegistry(): T

    fun stub(init: suspend T.() -> Unit) {
        if (registry == null) {
            registry = createRegistry()
        }
        runBlocking {
            registry!!.init()
        }
    }
}
