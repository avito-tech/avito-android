@file:Suppress("UnstableApiUsage")

package com.avito.kotlin.dsl

import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * https://github.com/gradle/gradle/issues/2016#issuecomment-492598038
 */
fun Property<RegularFile>.optionalIfNotExists(): Provider<RegularFile?> =
    flatMap {
        if (it.asFile.exists()) {
            Providers.of(it)
        } else {
            Providers.notDefined()
        }
    }
