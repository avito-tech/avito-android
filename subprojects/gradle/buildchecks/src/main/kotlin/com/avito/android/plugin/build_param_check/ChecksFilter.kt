package com.avito.android.plugin.build_param_check

import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.utils.logging.ciLogger
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.lang.IllegalStateException
import kotlin.reflect.full.createInstance

internal class ChecksFilter(
    private val project: Project,
    private val extension: BuildChecksExtension
) {

    fun checks(): List<Check> {
        return if (project.hasProperty(legacyEnabledGradleProperty)) {
            warnAboutLegacyProperties()

            val isEnabled = project.getBooleanProperty(legacyEnabledGradleProperty, false)
            if (isEnabled) {
                checksFromLegacyProperties()
            } else {
                project.ciLogger.info("Build checks are disabled")
                emptyList()
            }
        } else {
            enabledChecksFromExtension()
        }
    }

    private fun checksFromLegacyProperties(): List<Check> {
        val checks = allDefaultChecks(excluded = emptySet())

        // hardcoded value for backward compatibility
        checks.getInstance<Check.JavaVersion>().version = JavaVersion.VERSION_1_8

        checks.getInstance<Check.AndroidSdk>().apply {
            // disable the whole check to make simpler configuration
            enabled = project.getBooleanProperty(failOnSdkVersionMismatch, false)
            compileSdkVersion = checkNotNull(System.getProperty("compileSdkVersion").toIntOrNull())
            revision = project.getMandatoryIntProperty(androidJarRevision)
        }
        checks.getInstance<Check.GradleProperties>().enabled = true
        checks.getInstance<Check.ModuleTypes>().enabled = true

        return checks
    }

    private fun warnAboutLegacyProperties() {
        val deprecatedProperties = project.properties.keys
            .filter {
                deprecatedProperties.contains(it)
            }

        project.ciLogger.info(
            "WARNING: Properties $deprecatedProperties are deprecated. " +
                "Remove them and use '$extensionName {}' extension.\n" +
                "Checks in '$extensionName {}' won't work unless you do it."
        )
    }

    private val deprecatedProperties = listOf<String>(
        legacyEnabledGradleProperty,
        failOnSdkVersionMismatch,
        androidJarRevision
    )

    private fun enabledChecksFromExtension(): List<Check> {
        val enabledByUser = extension.checks
            .filter { it.enabled }

        return if (extension.enableByDefault) {
            enabledByUser + allDefaultChecks(excluded = extension.checks).filter { it.enabled }
        } else {
            enabledByUser
        }
    }

    private fun allDefaultChecks(excluded: Set<Check>): List<Check> {
        return Check::class.sealedSubclasses
            .filter {
                excluded.filterIsInstance(it.java).isEmpty()
            }
            .map { it.createInstance() }
    }
}

internal inline fun <reified T> Collection<Any>.hasInstance(): Boolean {
    return this.filterIsInstance(T::class.java).isNotEmpty()
}

internal inline fun <reified T> Collection<Any>.getInstance(): T {
    return this.filterIsInstance(T::class.java).first()
}
