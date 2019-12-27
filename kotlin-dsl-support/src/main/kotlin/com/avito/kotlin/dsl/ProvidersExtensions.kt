package com.avito.kotlin.dsl

import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

/**
 * В отличие от [ProviderFactory.provider] закеширует первое значение.
 */
@Suppress("UnstableApiUsage")
fun <T> ProviderFactory.lazy(factory: () -> T): Provider<T> = Providers.of(Unit).map { factory() }
