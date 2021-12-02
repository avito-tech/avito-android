package com.avito.android.signer

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal const val moduleName = "app"

internal const val applicationId = "com.app"

internal fun generateTestProject(
    testProjectDir: File,
    buildGradleKtsExtra: String,
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
                versionName = "22.1",
                plugins = plugins {
                    id("com.avito.android.sign-service")
                },
                buildGradleExtra = buildGradleKtsExtra
            )
        ),
    ).generateIn(testProjectDir)
}
