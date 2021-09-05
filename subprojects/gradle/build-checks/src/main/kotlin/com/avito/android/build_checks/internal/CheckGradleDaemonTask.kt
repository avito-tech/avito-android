package com.avito.android.build_checks.internal

import com.avito.android.build_checks.RootProjectChecksExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Properties

internal abstract class CheckGradleDaemonTask : DefaultTask() {

    @get:Input
    abstract val buildSrcDir: Property<String>

    @get:Input
    abstract val rootDir: Property<String>

    @TaskAction
    fun check() {
        checkSameVersions()
        checkSameDaemonArgs()
    }

    private fun checkSameVersions() {
        val buildSrcWrapperPropertiesFile = File(buildSrcDir.get(), "gradle/wrapper/gradle-wrapper.properties")

        if (!buildSrcWrapperPropertiesFile.exists()) {
            logger.info("No buildSrc in project, no check needed")
            return
        }

        val rootProperties = readProperties(File(rootDir.get(), "gradle/wrapper/gradle-wrapper.properties"))

        val buildSrcWrapperProperties = readProperties(buildSrcWrapperPropertiesFile)

        assertSameProperty("distributionUrl", rootProperties, buildSrcWrapperProperties) {
            "Different distributions of Gradle in root project and buildSrc " +
                "which may cause two daemons to be spawned when running buildSrc separately " +
                "(e.g. inside that directory). " +
                "Use the same distribution for both builds."
        }
    }

    private fun checkSameDaemonArgs() {
        val buildSrcPropertiesFile = File(buildSrcDir.get(), "gradle.properties")

        if (!buildSrcPropertiesFile.exists()) {
            logger.info("No buildSrc in project, no check needed")
            return
        }

        val rootProperties = readProperties(File(rootDir.get(), "gradle.properties"))

        val buildSrcProperties = readProperties(buildSrcPropertiesFile)

        assertSameProperty("org.gradle.jvmargs", rootProperties, buildSrcProperties) {
            "gradle.properties and buildSrc/gradle.properties have different org.gradle.jvmargs " +
                "which may cause two daemons to be spawned when running buildSrc separately " +
                "(e.g. inside that directory). " +
                "Use the same org.gradle.jvmargs for both builds."
        }
    }

    private fun assertSameProperty(property: String, left: Properties, right: Properties, error: () -> String) {
        require(left !== right)

        if (left.getProperty(property) != right.getProperty(property)) {
            throw GradleException(
                FailedCheckMessage(RootProjectChecksExtension::gradleDaemon, error()).toString()
            )
        }
    }

    private fun readProperties(propertiesFile: File) = Properties().apply {
        propertiesFile.inputStream().use { input ->
            load(input)
        }
    }
}
