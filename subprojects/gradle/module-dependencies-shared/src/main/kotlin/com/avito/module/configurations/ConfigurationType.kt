package com.avito.module.configurations

import org.gradle.api.artifacts.Configuration

public enum class ConfigurationType {

    Main {
        override fun toString(): String {
            return "ConfigurationType.Main"
        }
    },

    UnitTests {
        override fun toString(): String {
            return "ConfigurationType.UnitTests"
        }
    },

    AndroidTests {
        override fun toString(): String {
            return "ConfigurationType.AndroidTests"
        }
    },

    Lint {
        override fun toString(): String {
            return "ConfigurationType.Lint"
        }
    };

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

        private fun Configuration.isTest() = name.startsWith("test")
        private fun Configuration.isAndroidTest() = name.startsWith("androidTest")
        private fun Configuration.isLint() = name.startsWith("lint")
    }
}
