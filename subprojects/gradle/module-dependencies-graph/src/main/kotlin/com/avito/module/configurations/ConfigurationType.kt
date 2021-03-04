package com.avito.module.configurations

import org.gradle.api.artifacts.Configuration

public sealed class ConfigurationType {

    public object Main : ConfigurationType() {
        override fun toString(): String {
            return "ConfigurationType.Main"
        }
    }

    public object UnitTests : ConfigurationType() {
        override fun toString(): String {
            return "ConfigurationType.UnitTests"
        }
    }

    public object AndroidTests : ConfigurationType() {
        override fun toString(): String {
            return "ConfigurationType.AndroidTests"
        }
    }

    public object Lint : ConfigurationType() {
        override fun toString(): String {
            return "ConfigurationType.Lint"
        }
    }

    public companion object {

        public fun of(configuration: Configuration): ConfigurationType {
            return when {
                configuration.isTest() -> UnitTests
                configuration.isAndroidTest() -> AndroidTests
                configuration.isLint() -> Lint
                /**
                 * TODO Now we define all unknown configurations as Main.
                 *  But should ignore or convert them to the concrete type
                 */
                else -> Main
            }
        }

        public fun values(): Set<ConfigurationType> {
            return setOf(Main, UnitTests, AndroidTests, Lint)
        }

        private fun Configuration.isTest() = name.startsWith("test")
        private fun Configuration.isAndroidTest() = name.startsWith("androidTest")
        private fun Configuration.isLint() = name.startsWith("lint")
    }
}
