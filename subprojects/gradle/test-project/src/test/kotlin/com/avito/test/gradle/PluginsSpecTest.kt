package com.avito.test.gradle

import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class PluginsSpecTest {

    @Test
    fun `formatting - plugin with only id`() {
        val plugin = plugins {
            id("plugin-id")
        }

        assertThat(plugin.getScriptRepresentation()).isEqualTo(
            """|plugins {
               |    id("plugin-id")
               |}""".trimMargin()
        )
    }

    @Test
    fun `formatting - plugin with version`() {
        val plugin = plugins {
            id("plugin-id").version("1.0")
        }

        assertThat(plugin.getScriptRepresentation()).isEqualTo(
            """|plugins {
               |    id("plugin-id") version("1.0")
               |}""".trimMargin()
        )
    }

    @Test
    fun `formatting - disabled plugin`() {
        val plugin = plugins {
            id("plugin-id").apply(false)
        }

        assertThat(plugin.getScriptRepresentation()).isEqualTo(
            """|plugins {
               |    id("plugin-id") apply(false)
               |}""".trimMargin()
        )
    }

    @Test
    fun `formatting - disabled plugin with version`() {
        val plugin = plugins {
            id("plugin-id").version("1.0").apply(false)
        }

        assertThat(plugin.getScriptRepresentation()).isEqualTo(
            """|plugins {
               |    id("plugin-id") version("1.0") apply(false)
               |}""".trimMargin()
        )
    }

    @Test
    fun `formatting - multiple specs`() {
        val firstSpec = plugins {
            id("one")
        }
        val secondSpec = plugins {
            id("two")
        }
        val plugins = firstSpec.plus(secondSpec)

        assertThat(plugins.getScriptRepresentation()).isEqualTo(
            """|plugins {
               |    id("one")
               |    id("two")
               |}""".trimMargin()
        )
    }

    @Test
    fun `empty plugin spec - empty string`() {
        val spec = plugins { }
        assertThat(spec.getScriptRepresentation()).isEmpty()
    }

    @Test
    fun `one classpath plugin - buildscript classpath with apply`() {
        val spec = plugins {
            classpathPlugin("artifact:4.1.2", "plugin.id")
        }
        assertThat(spec.getScriptRepresentation()).isEqualTo("""
            |buildscript {
            |   dependencies {
            |       classpath("artifact:4.1.2")
            |   }
            |}
            |apply {
            |   plugin("plugin.id")
            |}
        """.trimMargin())
    }

    @Test
    fun `all plugin types`() {
        val spec = plugins {
            id("one")
            classpathPlugin("artifact:4.1.2", "plugin.id")
        }
        assertThat(spec.getScriptRepresentation()).isEqualTo("""
            |buildscript {
            |   dependencies {
            |       classpath("artifact:4.1.2")
            |   }
            |}
            |plugins {
            |    id("one")
            |}
            |apply {
            |   plugin("plugin.id")
            |}
        """.trimMargin())
    }
}
