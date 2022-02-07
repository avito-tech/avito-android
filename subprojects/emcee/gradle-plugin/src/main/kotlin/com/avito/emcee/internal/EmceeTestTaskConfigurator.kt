package com.avito.emcee.internal

import com.android.build.api.variant.ApplicationVariant
import com.avito.emcee.EmceeExtension
import com.avito.emcee.EmceeTestTask
import org.gradle.api.Action

internal interface EmceeTestTaskConfigurator {

    fun configure(task: EmceeTestTask)

    class Builder(emceeExtension: EmceeExtension) {

        private val configurators = mutableListOf<EmceeTestTaskConfigurator>(
            ExtensionConfigurator(emceeExtension)
        )

        fun application(variant: ApplicationVariant): Builder {
            configurators.add(ApplicationVariantConfigurator(variant))
            return this
        }

        fun build(): Action<EmceeTestTask> = Action { task ->
            configurators.forEach { it.configure(task) }
        }
    }
}
