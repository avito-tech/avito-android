package com.avito.jvm

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Verifies that `org.gradle.jvm.version` in every published Gradle module metadata file
 * equals the bytecode level promised by the module's JVM convention.
 * Catches a drifted compile target before the artifact is published.
 */
public abstract class CheckPublishedJvmVersionTask : DefaultTask() {

    @get:Input
    public abstract val expectedJvmVersion: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val moduleFiles: ConfigurableFileCollection

    @TaskAction
    public fun check() {
        val expected = expectedJvmVersion.get()
        val files = moduleFiles.files.filter { it.isFile }
        require(files.isNotEmpty()) {
            "No Gradle module metadata files found for ${project.path}; nothing to verify"
        }
        val problems = files.flatMap { file -> checkModuleFile(file, expected) }
        check(problems.isEmpty()) {
            buildString {
                appendLine("Published JVM version differs from the convention's bytecode level $expected:")
                problems.forEach { appendLine(" - $it") }
                appendLine(
                    "Consumers of this artifact rely on bytecode $expected; " +
                        "fix the compile target or the convention, not this check."
                )
            }
        }
        logger.lifecycle("Published org.gradle.jvm.version=$expected verified in ${files.size} module file(s)")
    }

    private fun checkModuleFile(file: File, expected: String): List<String> {
        val module = JsonSlurper().parse(file) as? Map<*, *>
            ?: error("Expected a JSON object at the top level of $file")
        val variants = module["variants"] as? List<*> ?: emptyList<Any>()
        return variants.mapNotNull { variant ->
            val variantMap = variant as? Map<*, *>
                ?: error("Expected a JSON object for a variant in $file, got ${variant?.javaClass?.name}")
            val name = variantMap["name"].toString()
            val attributes = variantMap["attributes"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val actual = attributes[JVM_VERSION_ATTRIBUTE]?.toString()
            when {
                actual == null && name in variantsWithJvmVersion ->
                    "$file: variant '$name' has no '$JVM_VERSION_ATTRIBUTE' attribute"

                actual != null && actual != expected ->
                    "$file: variant '$name' $JVM_VERSION_ATTRIBUTE=$actual, expected $expected"

                else -> null
            }
        }
    }

    private companion object {
        const val JVM_VERSION_ATTRIBUTE = "org.gradle.jvm.version"

        val variantsWithJvmVersion = setOf("apiElements", "runtimeElements")
    }
}
