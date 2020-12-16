@file:Suppress("UnstableApiUsage")

package com.avito.gradle.worker

import org.gradle.internal.hash.Hasher
import org.gradle.internal.isolation.Isolatable
import org.gradle.internal.snapshot.ValueSnapshot
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

fun WorkerExecutor.inMemoryWork(work: () -> Unit) {
    noIsolation().submit(NonSerializableWork::class.java) { params ->
        params.state = NonSerializationWorkerParams.StateHolder(work)
    }
}

internal abstract class NonSerializableWork : WorkAction<NonSerializationWorkerParams> {

    override fun execute() {
        parameters.state.work()
    }
}

internal abstract class NonSerializationWorkerParams : WorkParameters {

    abstract var state: StateHolder

    class StateHolder(val work: () -> Unit) : Isolatable<StateHolder> {

        override fun <S : Any?> coerce(type: Class<S>): S? {
            return null
        }

        override fun isolate(): StateHolder {
            return this
        }

        override fun appendToHasher(hasher: Hasher) {
            throw UnsupportedOperationException()
        }

        override fun asSnapshot(): ValueSnapshot {
            throw UnsupportedOperationException()
        }
    }
}
