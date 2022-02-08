package com.avito.android.build_checks.internal

import com.avito.android.AndroidSdk
import com.avito.android.build_checks.RootProjectChecksExtension
import com.avito.android.build_checks.RootProjectChecksExtension.RootProjectCheck.AndroidSdk.AndroidSdkVersion
import com.avito.utils.loadProperties
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

internal abstract class CheckAndroidSdkVersionTask @Inject constructor(
    projectLayout: ProjectLayout
) : DefaultTask() {

    @get:Internal
    val projectRootDir: Directory = projectLayout.projectDirectory

    @get:Input
    abstract val versions: SetProperty<AndroidSdkVersion>

    // synthetic output for up-to-date checks
    @OutputFile
    val output: Provider<RegularFile> = projectLayout.buildDirectory.file("reports/checkAndroidSdkVersionTask.out")

    @TaskAction
    fun check() {
        val versions = versions.get()
        versions.forEach {
            checkVersion(it)
        }
        output.get().asFile.writeText(versions.toString())
    }

    private fun checkVersion(version: AndroidSdkVersion) {
        val localRevision = localRevision(version.compileSdkVersion)
        val expectedRevision = version.revision

        if (localRevision < expectedRevision) {
            throw GradleException(
                FailedCheckMessage(
                    RootProjectChecksExtension::androidSdk,
                    """
                    You have an old Android SDK Platform version.
                    API level: ${version.compileSdkVersion}, 
                    (actual revision $localRevision, expected revision: $expectedRevision).
                    It breaks build caching (https://issuetracker.google.com/issues/117789774).
                    
                    How to fix: install or update Android SDK Platform in SDK Manager.
                    """.trimIndent()
                ).toString()
            )
        }
        if (localRevision > expectedRevision) {
            logger.error(
                FailedCheckMessage(
                    RootProjectChecksExtension::androidSdk,
                    """
                    You have a newer Android SDK Platform version.
                    API level: ${version.compileSdkVersion}, 
                    (actual revision $localRevision, expected revision: $expectedRevision).
                    It breaks build caching (https://issuetracker.google.com/issues/117789774).
                    
                    How to fix: update it in buildChecks config or in build environment.
                    """.trimIndent()
                ).toString()
            )
        }
    }

    private fun localRevision(compileSdkVersion: Int): Int {
        val sourceProperties: File = platformSourceProperties(compileSdkVersion)
        return requireNotNull(sourceProperties.loadProperties().getProperty("Pkg.Revision", null))
            .toInt()
    }

    private fun platformSourceProperties(compileSdkVersion: Int): File {
        return File(platformDir(compileSdkVersion), "source.properties")
    }

    private fun platformDir(compileSdkVersion: Int): File {
        val sdk = AndroidSdk.fromProject(
            rootDir = projectRootDir.asFile,
        )
        val dir = sdk.platform(compileSdkVersion)

        require(dir.exists()) {
            """========= ERROR =========
               Android SDK platform $compileSdkVersion is not found in ${dir.canonicalPath}.
               
               How to fix: install it or update in SDK Manager
                """.trimIndent()
        }
        return dir
    }
}
