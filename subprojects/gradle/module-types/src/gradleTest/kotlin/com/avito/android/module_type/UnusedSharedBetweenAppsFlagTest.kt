package com.avito.android.module_type

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test

internal class UnusedSharedBetweenAppsFlagTest : BaseModuleTypesTest() {

    @Test
    fun `shared flag without cross-app consumer - check fails`() {
        TestProjectGenerator(
            imports = listOf("import com.avito.android.module_type.*"),
            plugins = plugins {
                id(pluginId)
            },
            buildGradleExtra = """
                moduleTypes {
                    dependencyRestrictions {
                        defaultSeverity.set(com.avito.android.module_type.Severity.fail)
                        betweenDifferentApps(
                            reason = "Because betweenDifferentApps",
                            commonApp = CommonApp,
                            sharingApps = setOf(AppA, AppB)
                        )
                    }
                }
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    "shared",
                    imports = listOf("import com.avito.android.module_type.*"),
                    plugins = plugins {
                        id(pluginId)
                    },
                    buildGradleExtra = """
                        module {
                            type.set(ModuleType(AppA, FunctionalType.Library))
                            sharedBetweenApps.set(true)
                        }
                        """.trimIndent(),
                    useKts = true
                ),
            ),
            useKts = true
        ).generateIn(projectDir)

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Found unused sharedBetweenApps flags")
            .outputContains("no module of another application depends on them directly")
    }
}
