package com.avito.android.build_checks.internal.params

import com.avito.android.Result
import com.avito.android.build_checks.internal.BuildEnvironmentInfo
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.util.Properties

// TODO: merge into checker
internal class GradlePropertiesCheck(
    private val project: Project,
    private val envInfo: BuildEnvironmentInfo
) : ParameterCheck {

    // TODO: use a allow-list and pass it through extension
    private val ignoredParams = setOf(
        "artifactory_deployer",
        "artifactory_deployer_password",
        "android.builder.sdkDownload"
    )

    override fun getMismatches(): Result<Collection<ParameterMismatch>> {
        val references = readReferenceValues(project)
        return references.map {
            val mismatches = mutableListOf<ParameterMismatch>()
            it.forEach { entry ->
                val param = entry.key
                val expected = entry.value

                val mismatch = if (param.isSystemProperty()) {
                    systemPropertyMismatch(envInfo, param, expected)
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

    private fun systemPropertyMismatch(
        envInfo: BuildEnvironmentInfo,
        name: String,
        expected: String
    ): ParameterMismatch? {
        val actual = envInfo.getSystemProperty(normalizedSystemProperty(name)) ?: return null
        return if (actual != expected) {
            ParameterMismatch(name, expected, actual)
        } else {
            null
        }
    }

    private fun projectPropertyMismatch(name: String, expected: String): ParameterMismatch? {
        val actual = project.getOptionalStringProperty(name) ?: return null
        return if (actual != expected) {
            ParameterMismatch(name, expected, actual)
        } else {
            null
        }
    }

    private fun readReferenceValues(project: Project): Result<Map<String, String>> =
        Result.tryCatch {
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
