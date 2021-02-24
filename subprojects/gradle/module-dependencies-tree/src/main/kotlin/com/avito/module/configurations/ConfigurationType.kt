package com.avito.module.configurations

import org.gradle.api.artifacts.Configuration
import java.util.function.Predicate

enum class ConfigurationType : Predicate<Configuration> {
    IMPLEMENTATION {
        override fun test(configuration: Configuration) = configuration.isImplementation()
    },
    UNIT_TESTS {
        override fun test(configuration: Configuration) = configuration.isTest()
    },
    ANDROID_TESTS {
        override fun test(configuration: Configuration) = configuration.isAndroidTest()
    },
    LINT {
        override fun test(configuration: Configuration) = configuration.isLint()
    };

    protected fun Configuration.isImplementation() = !(isTest() or isAndroidTest() or isLint())
    protected fun Configuration.isTest(): Boolean = name.startsWith("test")
    protected fun Configuration.isAndroidTest(): Boolean = name.startsWith("androidTest")
    protected fun Configuration.isLint(): Boolean = name.startsWith("lint")
}
