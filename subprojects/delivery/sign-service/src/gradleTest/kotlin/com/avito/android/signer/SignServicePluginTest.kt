package com.avito.android.signer

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class SignServicePluginTest {

    @Test
    fun `direct task call - failed - no token specified`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |signer {
                |   serviceUrl.set("http://signer")
                |   useTls.set(false)
                |}
                |""".trimMargin()
        )

        ciRun(
            testProjectDir,
            "signApkViaServiceRelease",
            dryRun = true,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Task 'signApkViaServiceRelease' not found in root project 'test-project'")
    }

    @Test
    fun `plugin configuration - success - with both tokens set`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |signer {
                |   serviceUrl.set("http://signer")
                |   useTls.set(false)
                |   apkSignTokens.put("$applicationId", "12345")
                |   bundleSignTokens.put("$applicationId", "23456")
                |}
                |""".trimMargin()
        )

        ciRun(
            testProjectDir,
            "tasks",
            dryRun = false,
        ).assertThat()
            .buildSuccessful()
            .outputContains("signApkViaServiceRelease")
            .outputContains("signBundleViaServiceRelease")
            // has suffixed applicationId's
            .outputDoesNotContain("signApkViaServiceDebug")
            .outputDoesNotContain("signBundleViaServiceDebug")
            .outputDoesNotContain("signApkViaServiceStaging")
            .outputDoesNotContain("signBundleViaServiceStaging")
    }

    @Test
    fun `plugin apply - fails on configuration - serviceUrl not set`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |signer {
                |    useTls.set(false)
                |    bundleSignTokens.put("$applicationId", "12345")
                |}
                |""".trimMargin()
        )

        ciRun(
            testProjectDir,
            "tasks",
            dryRun = false,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("serviceUrl is invalid")
    }

    @Test
    fun `run sign bundle task - depends on package bundle task`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |android {
                |   buildTypes {
                |       getByName("release") {
                |           signingConfig = null
                |       }
                |   }
                |}
                |
                |signer {
                |   serviceUrl.set("http://signer")
                |   useTls.set(false)
                |   bundleSignTokens.put("$applicationId", "12345")
                |}
                |""".trimMargin()
        )

        ciRun(
            testProjectDir,
            ":$moduleName:signBundleViaServiceRelease",
            dryRun = true,
        ).assertThat()
            .buildSuccessful()
            .run {
                tasksShouldBeTriggered(
                    ":$moduleName:packageReleaseBundle",
                    ":$moduleName:signBundleViaServiceRelease"
                ).inOrder()
            }
    }

    @Test
    fun `run sign apk task - depends on package apk task`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |android {
                |   buildTypes {
                |       getByName("release") {
                |           signingConfig = null
                |       }
                |   }
                |}
                |
                |signer {
                |   serviceUrl.set("http://signer")
                |   useTls.set(false)
                |   apkSignTokens.put("$applicationId", "12345")
                |}
                |""".trimMargin()
        )

        ciRun(
            testProjectDir,
            ":$moduleName:signApkViaServiceRelease",
            dryRun = true,
        ).assertThat()
            .buildSuccessful()
            .run {
                tasksShouldBeTriggered(
                    ":$moduleName:packageRelease",
                    ":$moduleName:signApkViaServiceRelease"
                ).inOrder()
            }
    }

    /**
     * That's not an ideal behavior, should be a more meaningful message if possible
     */
    @Test
    fun `bundle task configuration - fails - configuration without token value set`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |signer {
                |   serviceUrl.set("http://signer")
                |   useTls.set(false)
                |   bundleSignTokens.put("$applicationId", providers.gradleProperty("signToken")) // no value
                |}
                |""".trimMargin()
        )

        ciRun(
            testProjectDir,
            ":$moduleName:signBundleViaServiceRelease",
            dryRun = true,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("'signBundleViaServiceRelease' not found in project ':app'")
    }

    @Test
    fun `plugin apply - fails if tls plugin is not applied to root project and mTLS is enabled`(
        @TempDir testProjectDir: File
    ) {
        TestProjectGenerator(
            useKts = true,
            modules = listOf(
                AndroidAppModule(
                    useKts = true,
                    name = moduleName,
                    packageName = applicationId,
                    enableKotlinAndroidPlugin = false,
                    versionCode = 100,
                    versionName = "219.0",
                    plugins = plugins {
                        id("com.avito.android.sign-service")
                    },
                    buildGradleExtra = """
                        |signer {
                        |   serviceUrl.set("http://signer")
                        |   apkSignTokens.put("$applicationId", "12345")
                        |}
                        |""".trimMargin()
                )
            ),
        ).generateIn(testProjectDir)

        ciRun(
            testProjectDir,
            ":$moduleName:tasks",
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Apply com.avito.android.tls-configuration plugin to the root project")
    }
}
