package com.avito.android.utils

import org.intellij.lang.annotations.Language

@Language("toml")
val LIBS_VERSIONS_TOML_CONTENT = """
            [versions]
            # https://developer.android.com/jetpack/androidx/releases/core
            androidx = "1.6.0"            
            detekt = "1.21.0"

            [plugins]
            detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }

            [libraries]
            gson = { module = "com.google.code.gson:gson", version = { strictly = "2.9.1" } }
            androidx-core = { module = "androidx.core:core", version.ref = "androidx" }
            androidx-constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
        """.trimIndent()

@Language("toml")
val LIBS_OWNERS_TOML_CONTENT = """
            [plugins]
            detekt = "Speed"

            [libraries]
            gson = "Speed"
            androidx-core = "Messenger"
            androidx-constraintLayout = "Messenger"
        """.trimIndent()
