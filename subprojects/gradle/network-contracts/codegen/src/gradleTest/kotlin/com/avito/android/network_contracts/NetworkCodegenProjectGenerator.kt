package com.avito.android.network_contracts

import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

object NetworkCodegenProjectGenerator {

    fun generate(
        projectDir: File,
        serviceUrl: String = "www.avito.ru/",
        generatedClassesPackage: String = "com.example",
        skipValidation: Boolean = true,
    ) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.network-contracts")
                id("com.avito.android.tls-configuration")
            },
            imports = listOf(),
            buildGradleExtra = """
                ${createMtlsExtensionString()}   
                networkContractsRoot {
                    useTls.set(false)
                    serviceUrl.set("$serviceUrl")
                    crtEnvName.set("test_env")
                    keyEnvName.set("test_key")
                }
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.network-contracts")
                    },
                    buildGradleExtra = """
                        networkContracts { 
                            kind.set("test-kind")
                            projectName.set("avito-app")
                            packageName.set("$generatedClassesPackage")
                            skipValidation.set($skipValidation)
                        } 
                    """.trimIndent(),
                    useKts = true
                ),
            ),
            useKts = true
        ).generateIn(projectDir)
    }
}
