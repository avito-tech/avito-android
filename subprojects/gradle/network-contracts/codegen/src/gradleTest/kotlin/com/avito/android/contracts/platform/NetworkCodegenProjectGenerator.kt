package com.avito.android.contracts.platform

import com.avito.android.contracts.platform.scheme.imports.ApiSchemesFilesGenerator
import com.avito.android.contracts.platform.scheme.imports.data.models.SchemaEntry
import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.plugin.plugins
import org.intellij.lang.annotations.Language
import java.io.File

internal const val DEFAULT_APP_NAME = "avito-android-test-app"
internal const val DEFAULT_GENERATED_PACKAGE = "com.example.test"
internal const val DEFAULT_API_SCHEMES_DIRECTORY = "src/main/resources/"
internal const val DEFAULT_BUILD_DIRECTORY = "build/networkContracts/codegen"

internal fun defaultAndroidModule(
    name: String = "impl",
    variants: List<NetworkCodegenProjectGenerator.Variant> = listOf(
        NetworkCodegenProjectGenerator.Variant(
            name = "test",
            appName = DEFAULT_APP_NAME,
            generatedClassesPackage = DEFAULT_GENERATED_PACKAGE,
            apiSchemesDirectory = DEFAULT_API_SCHEMES_DIRECTORY,
            generatedDirectory = DEFAULT_BUILD_DIRECTORY,
            skipValidation = true,
            validationByCodegen = true,
        )
    ),
    buildExtra: String = "",
) = AndroidLibModule(
    name = name,
    plugins = plugins {
        id("com.avito.android.schemes-contracts")
    },
    buildGradleExtra = buildGradleExtra(
        variants,
        buildExtra,
    ),
    useKts = true
)

internal fun defaultModule(
    name: String = "impl",
    variants: List<NetworkCodegenProjectGenerator.Variant> = listOf(
        NetworkCodegenProjectGenerator.Variant(
            name = "test",
            appName = DEFAULT_APP_NAME,
            generatedClassesPackage = DEFAULT_GENERATED_PACKAGE,
            apiSchemesDirectory = DEFAULT_API_SCHEMES_DIRECTORY,
            generatedDirectory = DEFAULT_BUILD_DIRECTORY,
            skipValidation = true,
            validationByCodegen = true,
        )
    ),
    buildExtra: String = "",
): KotlinModule {
    return KotlinModule(
        name = name,
        plugins = plugins {
            id("com.avito.android.schemes-contracts")
        },
        buildGradleExtra = buildGradleExtra(
            variants = variants,
            buildExtra = buildExtra
        ),
        useKts = true
    )
}

@Language("kotlin")
private fun buildGradleExtra(
    variants: List<NetworkCodegenProjectGenerator.Variant>,
    @Language("kotlin") buildExtra: String = ""
): String {
    val variantsConfigurations = variants.joinToString(separator = "\n") { variant ->
        """
            
           kind.set("test-kind")
           projectName.set("${variant.appName}")
           schemesBaseDirectory.set(project.layout.projectDirectory.dir("${variant.apiSchemesDirectory}"))
                            
           codegen.packageName.set("${variant.generatedClassesPackage}")
           codegen.skipValidation.set(${variant.skipValidation})
           codegen.generatedDirectory.set(project.layout.projectDirectory.dir("${variant.generatedDirectory}"))
           
           ${if (variant.validationEnabled) "validations.register(\"${variant.name}\")" else ""}
           imports.register("${variant.name}")
        """.trimIndent()
    }
    return """
        contracts {
            $variantsConfigurations
        }
        $buildExtra
    """.trimIndent()
}

object NetworkCodegenProjectGenerator {

    fun generate(
        projectDir: File,
        variants: List<Variant>,
        modules: List<Module> = listOf(defaultModule(variants = variants)),
        @Language("kotlin") buildExtra: String = ""
    ) {
        generate(projectDir, variants, { modules }, buildExtra)
    }

    fun generate(
        projectDir: File,
        variants: List<Variant> = listOf(Variant()),
        modules: (List<Variant>) -> List<Module> = { listOf(defaultModule(variants = it)) },
        @Language("kotlin") buildExtra: String = ""
    ) {

        val variantsConfigurations = variants.joinToString(separator = "\n") { variant ->
            """
                networks.register("${variant.name}") {
                    useTls.set(false)
                    serviceUrl.set("${variant.serviceUrl}")
                    crtEnvName.set("test_env")
                    keyEnvName.set("test_key")
                    timeouts.set(Timeouts.of(Duration.ofSeconds(10)))
                }
                               
                ${if (variant.fixationEnabled) "fixations.register(\"${variant.name}\")" else ""}
        """.trimIndent()
        }
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.schemes-contracts-root")
                id("com.avito.android.tls-configuration")
            },
            imports = listOf(
                "import com.avito.android.contracts.platform.extension.configurations.network.Timeouts",
                "import java.time.Duration"
            ),
            buildGradleExtra = """
                ${createMtlsExtensionString()}   
                contractsRoot {
                    $variantsConfigurations
                }
                $buildExtra
            """.trimIndent(),
            modules = modules.invoke(variants),
            useKts = true
        ).generateIn(projectDir)
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun generateSchemes(
        projectDir: File,
        moduleName: String? = null,
        apiSchemesDirectory: String = DEFAULT_API_SCHEMES_DIRECTORY,
        schemes: List<SchemaEntry> = emptyList(),
    ): List<File> {
        if (schemes.isEmpty()) {
            return emptyList()
        }

        val apiSchemesDir = File(
            projectDir,
            "$apiSchemesDirectory/api-clients"
        )
        val generatedFiles = ApiSchemesFilesGenerator(apiSchemesDir).generateFiles(schemes)
        val codegenFile = File(projectDir, "codegen.toml")
        codegenFile.createNewFile()
        codegenFile.writeText("codegen")
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

    data class Variant(
        val name: String = "test",
        val serviceUrl: String = "www.example.com/",
        val appName: String = DEFAULT_APP_NAME,
        val generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        val skipValidation: Boolean = true,
        val apiSchemesDirectory: String = DEFAULT_API_SCHEMES_DIRECTORY,
        val generatedDirectory: String = DEFAULT_BUILD_DIRECTORY,
        val validationByCodegen: Boolean = false,
        val validationEnabled: Boolean = true,
        val fixationEnabled: Boolean = true,
    )
}
