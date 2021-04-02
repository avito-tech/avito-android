package com.avito.ci

import com.avito.android.plugin.artifactory.artifactoryAppBackupTaskName
import com.avito.android.plugin.artifactory.artifactoryPasswordParameterName
import com.avito.android.plugin.artifactory.artifactoryUserParameterName
import com.avito.cd.uploadCdBuildResultTaskName
import com.avito.ci.steps.verifyTaskName
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.file
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.avito.upload_to_googleplay.deployTaskName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class CiStepsPluginTest {

    private lateinit var projectDir: File

    @Suppress("MaxLineLength")
    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "appA",
                    dependencies = setOf(project(":shared")),
                    plugins = plugins {
                        id("com.avito.android.signer")
                        id("com.avito.android.instrumentation-tests")
                        id("com.avito.android.prosector")
                        id("com.avito.android.qapps")
                        id("com.avito.android.artifactory-app-backup")
                        id("com.avito.android.cd")
                        id("org.gradle.maven-publish")
                    },
                    customScript = """
                            import com.avito.cd.BuildVariant
                            ${registerUiTestConfigurations("regress", "pr")}
                            signService {
                                bundle(android.buildTypes.release, "no_matter")
                                host("https://signer/")
                            }
                            prosector {
                                host("https://prosector")
                            }
                            qapps {
                                serviceUrl.set("https://qapps")
                                branchName.set("develop")
                                comment.set("build#1")
                            }
                            android {
                                buildTypes {
                                    release {
                                        minifyEnabled true
                                        proguardFile("proguard.pro")
                                    }
                                }
                            }
                            builds {
                                release {

                                    uiTests {
                                        configurations "regress"
                                    }
                                    unitTests { }
                                    lint { }

                                    artifacts {
                                        apk("debugApk", BuildVariant.DEBUG, "com.appA", "${'$'}{project.buildDir}/outputs/apk/debug/appA-debug.apk") {}
                                        apk("releaseApk", BuildVariant.RELEASE, "com.appA", "${'$'}{project.buildDir}/outputs/apk/release/appA-release.apk") {}
                                        bundle("releaseBundle", BuildVariant.RELEASE, "com.appA", "${'$'}{project.buildDir}/outputs/release/debug/appA-release.aab") {}
                                        mapping("releaseMapping", BuildVariant.RELEASE, "${'$'}{project.buildDir}/reports/mapping.txt")
                                        file("nonExistedJson","${'$'}{project.buildDir}/reports/not-existed-file.json")
                                    }


                                    uploadToQapps {
                                        artifacts = ['releaseApk']
                                    }
                                    uploadToProsector {
                                        artifacts = ['debugApk']
                                    }
                                    uploadToArtifactory {
                                        artifacts = ['debugApk', 'releaseApk']
                                    }
                                    uploadBuildResult {
                                        uiTestConfiguration = "regress"
                                    }
                                    deploy {}
                                }
                            }
                        """.trimIndent()
                ),
                AndroidAppModule(
                    name = "appB",
                    dependencies = setOf(project(":shared")),
                    plugins = plugins {
                        id("com.avito.android.cd")
                    }
                ),
                AndroidLibModule(
                    name = "shared",
                    dependencies = setOf(project(":transitive"))
                ),
                AndroidLibModule(
                    name = "transitive"
                ),
                AndroidLibModule(
                    name = "independent"
                )
            )
        ).generateIn(projectDir)
    }

    @TestFactory
    fun `assemble should not trigger artifacts tasks`(): List<DynamicTest> {
        val result = runTask(":appA:assemble")

        return listOf(
            ":appA:releaseCopyArtifacts",
            ":appA:releaseVerifyArtifacts"
        ).map { task ->
            dynamicTest("$task should not be triggered on dev build, it's CI only") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should trigger project release assemble task`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:packageRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should not trigger assemble task for unnecessary build types`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:assembleStaging"
        ).map { task ->
            dynamicTest("$task should not be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should trigger artifacts tasks`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:releaseCopyArtifacts",
            ":appA:releaseVerifyArtifacts"
        ).map { task ->
            dynamicTest("$task should be triggered by release tasks") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should not trigger sibling project tasks`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appB",
            ":independent"
        ).map { module ->
            dynamicTest("$module tasks should not be triggered by :appA:release") {
                result.assertThat().moduleTaskShouldNotBeTriggered(module)
            }
        }
    }

    @TestFactory
    fun `release should trigger project lint task`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:lintRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().moduleTaskShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should run test for all dependant modules`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:test",
            ":shared:test",
            ":transitive:test"
        ).map { task ->
            dynamicTest("$task task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should trigger project debug package tasks as it is necessary for ui tests`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:packageDebug",
            ":appA:packageDebugAndroidTest"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should specified ui test configurations`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:instrumentationRegress"
        ).map { task ->
            dynamicTest("$task task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should not trigger all ui test configurations`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:instrumentationPrDebug"
        ).map { task ->
            dynamicTest("$task task should NOT be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release builds and signs bundle`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:bundleRelease",
            ":appA:signBundleViaServiceRelease"
        ).map { task ->
            dynamicTest("$task task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release signs release apk via service`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:signApkViaServiceRelease"
        ).map { task ->
            dynamicTest("$task task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release does not sign apk for unnecessary build types`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:signApkViaServiceStaging"
        ).map { task ->
            dynamicTest("$task task should not be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release uploads debug apk to prosector`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:prosectorUploadDebug"
        ).map { task ->
            dynamicTest("$task task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release uploads release apk to qapps`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:qappsUploadRelease"
        ).map { task ->
            dynamicTest("$task task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release does not upload apk for unnecessary build types`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:qappsUploadDebug",
            ":appA:qappsUploadStaging"
        ).map { task ->
            dynamicTest("$task should NOT be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `assembleDebug should not trigger CI tasks`(): List<DynamicTest> {
        val result = runTask(":appA:assembleDebug")

        return listOf(
            ":appA:signViaService"
        ).map { task ->
            dynamicTest("$task should not be triggered on dev build, it's CI only") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `assembleDebug should not trigger release tasks`(): List<DynamicTest> {
        val result = runTask(":appA:assembleDebug", dryRun = true)

        return listOf(
            ":appA:assembleRelease"
        ).map { task ->
            dynamicTest("$task should not be triggered on dev build, it's CI only") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @Test
    fun `sendCdBuildResult Triggered after publish and instrumentationTests and deployToGooglePlay`() {
        val configFileName = "xxx"
        val cdBuildConfig = """
        {
            "schema_version": 1,
            "release_version": "248.0",
            "output_descriptor": {
                "path": "http://foo.bar",
                "skip_upload": false
            },
            "deployments": [
                {
                    "type": "google-play",
                    "artifact_type": "bundle",
                    "build_variant": "release",
                    "track": "alpha"
                }
            ]
        }
        """
        val configFile = projectDir.file(configFileName)
        configFile.writeText(cdBuildConfig)
        val result = runTask(":appA:release", "-Pcd.build.config.file=$configFileName")

        result
            .assertThat().apply {
                tasksShouldBeTriggered(
                    ":appA:instrumentationRegress",
                    ":appA:$artifactoryAppBackupTaskName",
                    ":appA:$deployTaskName",
                    ":appA:$uploadCdBuildResultTaskName"
                ).inOrder()
                buildSuccessful()
            }
    }

    @Test
    fun `sendCdBuildResult Triggered after publish and instrumentationTests and without deployTask`() {
        val configFileName = "xxx"
        val cdBuildConfig = """
        {
            "schema_version": 1,
            "release_version": "248.0",
            "output_descriptor": {
                "path": "http://foo.bar",
                "skip_upload": false
            },
            "deployments": []
        }
        """
        val configFile = projectDir.file(configFileName)
        configFile.writeText(cdBuildConfig)
        val result = runTask(":appA:release", "-Pcd.build.config.file=$configFileName")

        result
            .assertThat().apply {
                tasksShouldBeTriggered(
                    ":appA:instrumentationRegress",
                    ":appA:$artifactoryAppBackupTaskName",
                    ":appA:$uploadCdBuildResultTaskName"
                ).inOrder()
                buildSuccessful()
            }
    }

    @Test
    fun `sendCdBuildResult not Triggered`() {
        val result = runTask(":appA:release")

        result
            .assertThat().apply {
                tasksShouldNotBeTriggered(":appA:$uploadCdBuildResultTaskName")
                buildSuccessful()
            }
    }

    @Test
    fun `verify artifacts fails if file is missed`() {
        val result = runTask(":appA:releaseVerifyArtifacts", dryRun = false, expectedFailure = true)

        result.assertThat().run {
            buildFailed().outputContains("Artifact: appA/build/reports/not-existed-file.json not found")
        }
    }

    @Test
    fun `deployToGooglePlay Triggered after verifyArtifactsTask`() {
        val configFileName = "xxx"
        val buildType = "release"
        val cdBuildConfig = """
        {
            "schema_version": 1,
            "release_version": "248.0",
            "output_descriptor": {
                "path": "http://foo.bar",
                "skip_upload": true
            },
            "deployments": [
                {
                    "type": "google-play",
                    "artifact_type": "bundle",
                    "build_variant": "release",
                    "track": "alpha"
                }
            ]
        }
        """
        val file = projectDir.file(configFileName)
        file.createNewFile()
        file.writeText(cdBuildConfig)
        val result = runTask(":appA:$buildType", "-Pcd.build.config.file=$configFileName")

        result
            .assertThat().apply {
                tasksShouldBeTriggered(
                    ":appA:${verifyTaskName(buildType)}",
                    ":appA:$deployTaskName"
                ).inOrder()
                buildSuccessful()
            }
    }

    @Test
    fun `Configuration failed when deployments have two deployment with same build variant`() {
        val configFileName = "xxx"
        val buildType = "release"
        val cdBuildConfig = """
        {
            "schema_version": 1,
            "release_version": "248.0",
            "output_descriptor": {
                "path": "http://foo.bar",
                "skip_upload": true
            },
            "deployments": [
                {
                    "type": "google-play",
                    "artifact_type": "bundle",
                    "build_variant": "release",
                    "track": "alpha"
                },
                {
                    "type": "google-play",
                    "artifact_type": "apk",
                    "build_variant": "release",
                    "track": "beta"
                }
            ]
        }
        """
        val file = projectDir.file(configFileName)
        file.createNewFile()
        file.writeText(cdBuildConfig)
        val result = runTask(":appA:$buildType", "-Pcd.build.config.file=$configFileName", expectedFailure = true)

        result
            .assertThat()
            .buildFailed()
            .outputContains("Must be one deploy per variant")
    }

    @Test
    fun `deployToGooglePlay not Triggered`() {
        val result = runTask(":appA:release")
        result.assertThat().apply {
            tasksShouldNotBeTriggered(":appA:$deployTaskName")
            buildSuccessful()
        }
    }

    private fun runTask(
        vararg args: String,
        dryRun: Boolean = true,
        expectedFailure: Boolean = false
    ) = runTask(
        projectDir,
        *args,
        "-PartifactoryUrl=http://artifactory",
        "-P$artifactoryUserParameterName=user",
        "-P$artifactoryPasswordParameterName=password",
        dryRun = dryRun,
        expectedFailure = expectedFailure
    )
}
