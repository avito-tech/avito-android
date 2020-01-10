package com.avito.android.plugin.build_param_check

import com.avito.kotlin.dsl.getOptionalStringProperty
import org.funktionale.tries.Try
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.util.Properties

internal class GradlePropertiesCheck(private val project: Project) : Check {

    private val ignoredParams = setOf(
        "artifactory_deployer",
        "artifactory_deployer_password",
        "android.builder.sdkDownload"
    )

    override fun getMismatches(): Try<Collection<Mismatch>> {
        val references = readReferenceValues(project)
        return references.map {
            val mismatches = mutableListOf<Mismatch>()
            it.forEach { entry ->
                val param = entry.key
                val expected = entry.value

                val mismatch = if (param.isSystemProperty()) {
                    systemPropertyMismatch(param, expected)
                } else {
                    projectPropertyMismatch(param, expected)
                }
                if (mismatch != null) {
                    mismatches.add(mismatch)
                }
            }
            mismatches
        }
    }

    private fun systemPropertyMismatch(name: String, expected: String): Mismatch? {
        val actual = System.getProperty(normalizedSystemProperty(name)) ?: return null
        return if (actual != expected) {
            Mismatch(name, expected, actual)
        } else {
            null
        }
    }

    private fun projectPropertyMismatch(name: String, expected: String): Mismatch? {
        val actual = project.getOptionalStringProperty(name) ?: return null
        return if (actual != expected) {
            Mismatch(name, expected, actual)
        } else {
            null
        }
    }

    private fun readReferenceValues(project: Project): Try<Map<String, String>> =
        Try {
            FileInputStream(File(project.rootDir, "gradle.properties")).use { properties ->
                val references = Properties()
                references.load(properties)
                references.entries
                    .asSequence()
                    .map { it.key.toString() to it.value.toString() }
                    .filterNot { ignoredParams.contains(it.first) }
                    .toMap()
            }
        }

    private fun normalizedSystemProperty(property: String): String {
        return property.substringAfter("systemProp.", missingDelimiterValue = property)
    }

    private fun String.isSystemProperty() = startsWith(systemPropertyPrefix)
}

// https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties
private const val systemPropertyPrefix = "systemProp."
