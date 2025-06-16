package com.avito.kotlin.dsl

import org.gradle.api.provider.Provider

public fun <U, T1, T2, R> Provider<U>.zip(
    provider1: Provider<T1>,
    provider2: Provider<T2>,
    transformer: (U, T1, T2) -> R
): Provider<R> {
    return this.zip(provider1) { u, t1 -> u to t1 }
        .zip(provider2) { (u, t1), t2 -> transformer(u, t1, t2) }
}
