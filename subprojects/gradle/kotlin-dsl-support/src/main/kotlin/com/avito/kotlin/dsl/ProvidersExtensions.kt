package com.avito.kotlin.dsl

import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

@Suppress("UnstableApiUsage")
@Deprecated("It behaves like simple provider", replaceWith = ReplaceWith("[ProviderFactory.provider]"))
fun <T> ProviderFactory.lazy(factory: () -> T): Provider<T> = Providers.of(Unit).map { factory() }
