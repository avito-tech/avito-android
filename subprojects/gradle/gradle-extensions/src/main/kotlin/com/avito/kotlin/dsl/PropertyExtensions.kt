package com.avito.kotlin.dsl

import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * https://github.com/gradle/gradle/issues/2016#issuecomment-492598038
 */
public fun Property<RegularFile>.toOptional(): Provider<RegularFile?> =
    flatMap {
        if (it.asFile.exists() && it.asFile.length() > 0) {
            Providers.of(it)
        } else {
            Providers.notDefined()
        }
    }
