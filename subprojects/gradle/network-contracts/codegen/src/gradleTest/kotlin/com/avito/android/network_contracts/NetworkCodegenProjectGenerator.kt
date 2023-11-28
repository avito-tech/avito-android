package com.avito.android.network_contracts

import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

object NetworkCodegenProjectGenerator {

    fun generate(
        projectDir: File,
        serviceUrl: String,
        generatedClassesPackage: String = "com.example",
    ) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                ${createMtlsExtensionString()}
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.network-contracts")
                    },
                    imports = listOf(
                        "import com.avito.android.network_contracts.extension.urls.ServiceUrlConfiguration"
                    ),
                    buildGradleExtra = """
                        networkContracts { 
                            kind.set("kotlin")
                            projectName.set("avito-app")
                            codegenFilePath.set("${projectDir.path}/tmp/codegen.yml")
                            packageName.set("$generatedClassesPackage")
                            version.set("1.0.0")
                            useTls.set(false)
                            urls {
                                register(
                                    "${NetworkContractsModuleExtension.SERVICE_URL_NAME}", 
                                    ServiceUrlConfiguration::class.java
                                ) { 
                                    serviceUrl.set("$serviceUrl")
                                }
                            }
                        } 
                    """.trimIndent(),
                    useKts = true
                ),
            ),
            useKts = true
        ).generateIn(projectDir)
    }
}
