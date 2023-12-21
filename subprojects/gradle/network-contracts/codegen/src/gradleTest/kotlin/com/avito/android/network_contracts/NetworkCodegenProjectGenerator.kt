package com.avito.android.network_contracts

import com.avito.android.network_contracts.scheme.imports.ApiSchemesFilesGenerator
import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.plugin.plugins
import org.intellij.lang.annotations.Language
import java.io.File

internal const val DEFAULT_APP_NAME = "avito-android-test-app"
internal const val DEFAULT_GENERATED_PACKAGE = "com.example.test"

internal fun defaultModule(
    name: String = "impl",
    appName: String = DEFAULT_APP_NAME,
    generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
    skipValidation: Boolean = true,
    buildExtra: String = "",
) = listOf(
    KotlinModule(
        name = name,
        plugins = plugins {
            id("com.avito.android.network-contracts")
        },
        buildGradleExtra = """
               networkContracts { 
                   kind.set("test-kind")
                   projectName.set("$appName")
                   packageName.set("$generatedClassesPackage")
                   skipValidation.set($skipValidation)
               }
               $buildExtra
        """.trimIndent(),
        useKts = true
    ),
)

object NetworkCodegenProjectGenerator {

    fun generate(
        projectDir: File,
        serviceUrl: String = "www.avito.ru/",
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        skipValidation: Boolean = true,
        modules: List<Module> = defaultModule(
            generatedClassesPackage = generatedClassesPackage,
            skipValidation = skipValidation,
        ),
        @Language("kotlin") buildExtra: String = ""
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
                $buildExtra
            """.trimIndent(),
            modules = modules,
            useKts = true
        ).generateIn(projectDir)
    }

    internal fun generateSchemes(
        projectDir: File,
        moduleName: String? = null,
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        schemes: List<SchemaEntry> = emptyList(),
    ): List<File> {
        if (schemes.isEmpty()) {
            return emptyList()
        }

        val modulePath = moduleName?.let { "$it/" }.orEmpty()
        val packageDir = File(
            projectDir,
            "${modulePath}src/main/kotlin/" +
                generatedClassesPackage.replace(".", "/") +
                "/api-clients"
        )
        val generatedFiles = ApiSchemesFilesGenerator(packageDir).generateFiles(schemes)
        val codegenFile = File(packageDir.parentFile, "codegen.toml")
        codegenFile.createNewFile()
        return generatedFiles + codegenFile
    }

    internal fun generateCodegenFiles(
        moduleName: String,
        projectDir: File,
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        generatedFiles: List<File> = emptyList(),
    ): List<File> {
        if (generatedFiles.isEmpty()) {
            return emptyList()
        }

        val generatedDir = File(
            projectDir,
            "$moduleName/src/main/kotlin/" +
                generatedClassesPackage.replace(".", "/") +
                "/generated"
        )

        return generatedFiles.map {
            File(generatedDir, it.path).apply {
                parentFile.mkdirs()
                if (!exists()) {
                    createNewFile()
                }
            }
        }
    }
}
