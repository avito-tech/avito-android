package com.avito.android.plugin.build_param_check

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Properties

abstract class CheckGradleDaemonTask : DefaultTask() {

    @TaskAction
    fun check() {
        checkSameVersions()
        checkSameDaemonArgs()
    }

    private fun checkSameVersions() {
        val rootProperties = readProperties(File(project.rootDir, "gradle/wrapper/gradle-wrapper.properties"))
        val buildSrcProperties = readProperties(File(project.buildSrcDir, "gradle/wrapper/gradle-wrapper.properties"))

        assertSameProperty("distributionUrl", rootProperties, buildSrcProperties) {
            "Different distributions of Gradle in root project and buildSrc " +
                "which may cause two daemons to be spawned when running buildSrc separately (e.g. inside that directory). " +
                "Use the same distribution for both builds."
        }
    }

    private fun checkSameDaemonArgs() {
        val rootProperties = readProperties(File(project.rootDir, "gradle.properties"))

        //todo случай когда нет buildSrc
        val buildSrcProperties = readProperties(File(project.buildSrcDir, "gradle.properties"))

        assertSameProperty("org.gradle.jvmargs", rootProperties, buildSrcProperties) {
            "gradle.properties and buildSrc/gradle.properties have different org.gradle.jvmargs " +
                "which may cause two daemons to be spawned when running buildSrc separately (e.g. inside that directory). " +
                "Use the same org.gradle.jvmargs for both builds."
        }
    }

    private fun assertSameProperty(property: String, left: Properties, right: Properties, error: () -> String) {
        require(left !== right)

        if (left.getProperty(property) != right.getProperty(property)) {
            throw GradleException(error())
        }
    }

    private val Project.buildSrcDir: File
        get() = File(project.rootDir, "buildSrc")

    private fun readProperties(propertiesFile: File) = Properties().apply {
        propertiesFile.inputStream().use { input ->
            load(input)
        }
    }

}
