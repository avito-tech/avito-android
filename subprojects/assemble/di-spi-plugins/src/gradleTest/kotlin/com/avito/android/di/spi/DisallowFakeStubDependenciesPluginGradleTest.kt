package com.avito.android.di.spi

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Suppress("FunctionNaming")
class DisallowFakeStubDependenciesPluginGradleTest {

    private val buildGradleExtra by lazy {
        val rootDir = System.getProperty("rootDir")
        val projectVersion = System.getProperty("projectVersion")
        val spiPluginJarPath = "$rootDir/subprojects/assemble/di-spi-plugins/build/libs/" +
            "di-spi-plugins-$projectVersion.jar"

        check(File(spiPluginJarPath).exists()) {
            "$spiPluginJarPath not found"
        }

        """
            dependencies {
                implementation(libs.dagger)
                ksp(libs.dagger.compiler)
                ksp(files("$spiPluginJarPath"))
            }
            
            anvil {
                useKsp(
                    contributesAndFactoryGeneration = true,
                    componentMerging = true,
                )
            }
        """.trimIndent()
    }

    @Test
    fun `app uses impl module and build succeeds`(@TempDir projectDir: File) {
        runTest(projectDir, expectFailure = false, appDependsOnImpl = true)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    fun `app uses only public module and build fails`(@TempDir projectDir: File) {
        runTest(projectDir, expectFailure = true, appDependsOnImpl = false)
            .assertThat()
            .buildFailed()
            .outputContains("Component MergedApplicationComponent has forbidden provided type FakeA")
    }

    private fun runTest(
        projectDir: File,
        expectFailure: Boolean,
        appDependsOnImpl: Boolean
    ): TestResult {
        val publicModule = createPublicModule()
        val implModule = createImplModule()
        val appModule = createAppModule(appDependsOnImpl = appDependsOnImpl)
        val demoModule = createDemoModule()

        TestProjectGenerator(
            useRootLibsVersionsToml = true,
            modules = listOf(publicModule, implModule, appModule, demoModule)
        ).generateIn(projectDir)

        return gradlew(
            projectDir,
            ":app:assembleDebug",
            ":demo:assembleDebug",
            expectFailure = expectFailure,
            useModuleClasspath = false,
        )
    }

    private fun plugins() = plugins {
        alias("libs.plugins.anvil")
        alias("libs.plugins.ksp")
    }

    private fun createPublicModule() = AndroidLibModule(
        name = "public",
        packageName = "com.pub",
        plugins = plugins(),
        buildGradleExtra = buildGradleExtra,
        useKts = true,
        mutator = {
            dir("src/main/kotlin") {
                file("Public.kt", """
                    import com.squareup.anvil.annotations.ContributesBinding
                    import javax.inject.Inject

                    interface AppScope

                    interface A

                    @ContributesBinding(AppScope::class)
                    class FakeA @Inject constructor() : A
                """.trimIndent())
            }
        }
    )

    private fun createImplModule() = AndroidLibModule(
        name = "impl",
        plugins = plugins(),
        dependencies = setOf(project(":public", CONFIGURATION.API)),
        buildGradleExtra = buildGradleExtra,
        useKts = true,
        mutator = {
            dir("src/main/kotlin") {
                file("AImpl.kt", """
                    import com.squareup.anvil.annotations.ContributesBinding
                    import javax.inject.Inject

                    @ContributesBinding(AppScope::class, replaces = [FakeA::class])
                    class AImpl @Inject constructor() : A
                """.trimIndent())
            }
        }
    )

    private fun createAppModule(appDependsOnImpl: Boolean) = AndroidAppModule(
        name = "app",
        plugins = plugins(),
        dependencies = if (appDependsOnImpl) {
            setOf(project(":impl"))
        } else {
            setOf(project(":public"))
        },
        buildGradleExtra = buildGradleExtra,
        useKts = true,
        mutator = {
            dir("src/main/kotlin") {
                file("ApplicationComponent.kt", """
                    import com.squareup.anvil.annotations.MergeComponent

                    @MergeComponent(AppScope::class)
                    interface ApplicationComponent {
                        fun a(): A
                    }
                """.trimIndent())
                file("createComponent.kt", """
                    // to check that DaggerApplicationComponent is generated
                    fun createComponent(): ApplicationComponent {
                        return DaggerApplicationComponent.create()
                    }
                """.trimIndent())
            }
        }
    )

    private fun createDemoModule() = AndroidAppModule(
        name = "demo",
        plugins = plugins(),
        dependencies = setOf(project(":public")),
        buildGradleExtra = buildGradleExtra,
        useKts = true,
        mutator = {
            dir("src/main/kotlin") {
                file("DemoComponent.kt", """
                    import com.squareup.anvil.annotations.MergeComponent

                    @MergeComponent(AppScope::class)
                    interface DemoComponent {
                        fun a(): A
                    }
                """.trimIndent())
                file("createComponent.kt", """
                    // to check that DaggerDemoComponent is generated
                    fun createComponent(): DemoComponent {
                        return DaggerDemoComponent.create()
                    }
                """.trimIndent())
            }
        }
    )
}
