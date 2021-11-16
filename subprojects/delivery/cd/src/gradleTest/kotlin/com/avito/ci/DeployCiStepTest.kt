package com.avito.ci

import com.avito.android.plugin.artifactory.artifactoryAppBackupTaskName
import com.avito.cd.uploadCdBuildResultTaskName
import com.avito.ci.steps.verifyTaskName
import com.avito.test.gradle.file
import com.avito.upload_to_googleplay.deployTaskName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class DeployCiStepTest : BaseCiStepsPluginTest() {

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    @Suppress("MaxLineLength")
    fun `cd config with deployment - sendCdBuildResult triggered after publish, instrumentationTests, deployToGooglePlay`() {
        generateProjectWithConfiguredCiSteps()
        val buildVariant = "release"
        val cdBuildConfig = cdBuildConfig(buildVariant)
        val configFileName = writeCdBuildConfigFile(cdBuildConfig)
        val result = runTask(":appA:$buildVariant", "-Pcd.build.config.file=$configFileName")

        result
            .assertThat().apply {
                tasksShouldBeTriggered(
                    ":appA:instrumentationRegressDefault",
                    ":appA:$artifactoryAppBackupTaskName",
                    ":appA:$deployTaskName",
                    ":appA:$uploadCdBuildResultTaskName"
                ).inOrder()
                buildSuccessful()
            }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `cd config without deployment - sendCdBuildResult triggered after publish and instrumentationTests and without deployTask`() {
        generateProjectWithConfiguredCiSteps()
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
        val configFileName = writeCdBuildConfigFile(cdBuildConfig)
        val result = runTask(":appA:release", "-Pcd.build.config.file=$configFileName")

        result
            .assertThat().apply {
                tasksShouldBeTriggered(
                    ":appA:instrumentationRegressDefault",
                    ":appA:$artifactoryAppBackupTaskName",
                    ":appA:$uploadCdBuildResultTaskName"
                ).inOrder()
                buildSuccessful()
            }
    }

    @Test
    fun `no cd config - sendCdBuildResult not Triggered`() {
        generateProjectWithConfiguredCiSteps()
        val result = runTask(":appA:release")

        result
            .assertThat().apply {
                tasksShouldNotBeTriggered(":appA:$uploadCdBuildResultTaskName")
                buildSuccessful()
            }
    }

    @Test
    fun `valid cd config - deployToGooglePlay triggered after verifyArtifactsTask`() {
        generateProjectWithConfiguredCiSteps()
        val buildVariant = "release"
        val cdBuildConfig = cdBuildConfig(buildVariant)
        val configFileName = writeCdBuildConfigFile(cdBuildConfig)

        val result = runTask(":appA:$buildVariant", "-Pcd.build.config.file=$configFileName")

        result
            .assertThat().apply {
                tasksShouldBeTriggered(
                    ":appA:${verifyTaskName(buildVariant)}",
                    ":appA:$deployTaskName"
                ).inOrder()
                buildSuccessful()
            }
    }

    @Test
    fun `deployments have two deployment with same build variant - configuration failed`() {
        generateProjectWithConfiguredCiSteps()
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
        val configFileName = writeCdBuildConfigFile(cdBuildConfig)
        val result = runTask(":appA:$buildType", "-Pcd.build.config.file=$configFileName", expectedFailure = true)

        result
            .assertThat()
            .buildFailed()
            .outputContains("Must be one deploy per variant")
    }

    @Test
    fun `no cd config - deployToGooglePlay not triggered`() {
        generateProjectWithConfiguredCiSteps()
        val result = runTask(":appA:release")
        result.assertThat().apply {
            tasksShouldNotBeTriggered(":appA:$deployTaskName")
            buildSuccessful()
        }
    }

    @Test
    fun `upload proguard to crashlytics enabled - uploadCrashlyticsProguardFile executed`() {
        generateProjectWithConfiguredCiSteps()
        val buildVariant = "release"
        val cdBuildConfig = cdBuildConfig(buildVariant)
        val configFileName = writeCdBuildConfigFile(cdBuildConfig)

        val result = runTask(":appA:$buildVariant", "-Pcd.build.config.file=$configFileName")
        result.assertThat().apply {
            tasksShouldBeTriggered(":appA:uploadCrashlyticsMappingFileRelease")
            buildSuccessful()
        }
    }

    @Test
    fun `upload proguard to crashlytics disabled - build failed`() {
        generateProjectWithConfiguredCiSteps(uploadCrashlyticsMappingFileEnabled = false)
        val buildVariant = "release"
        val cdBuildConfig = cdBuildConfig(buildVariant)
        val configFileName = writeCdBuildConfigFile(cdBuildConfig)

        val result = runTask(":appA:$buildVariant", "-Pcd.build.config.file=$configFileName")
        result.assertThat().apply {
            tasksShouldNotBeTriggered(":appA:uploadCrashlyticsMappingFileRelease")
            buildSuccessful()
        }
    }

    private fun writeCdBuildConfigFile(config: String): String {
        val configFileName = "xxx"
        val file = projectDir.file(configFileName)
        file.createNewFile()
        file.writeText(config)
        return configFileName
    }

    private fun cdBuildConfig(
        @Suppress("SameParameterValue") buildVariant: String
    ): String {
        return """
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
                    "build_variant": "$buildVariant",
                    "track": "alpha"
                }
            ]
        }
        """
    }
}
