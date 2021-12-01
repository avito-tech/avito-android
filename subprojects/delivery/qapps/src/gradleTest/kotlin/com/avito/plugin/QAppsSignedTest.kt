package com.avito.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class QAppsSignedTest {

    @Test
    fun `qappsUploadSigned - depends on signer task`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "com.app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.sign-service")
                    },
                    useKts = true,
                    buildGradleExtra = """
                        |signer {
                        |   serviceUrl.set("http://stub")
                        |   apkSignTokens.put("com.app", "12345")
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            ":app:qappsUploadSignedRelease",
            dryRun = true
        ).assertThat().buildSuccessful()
            .tasksShouldBeTriggered(
                ":app:packageRelease",
                ":app:signApkViaServiceRelease",
                ":app:qappsUploadSignedRelease"
            )
            .inOrder()
    }
}
