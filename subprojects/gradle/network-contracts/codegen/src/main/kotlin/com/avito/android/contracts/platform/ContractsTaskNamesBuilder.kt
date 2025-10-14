package com.avito.android.contracts.platform

import com.avito.android.contracts.platform.scheme.codegen.CodegenTask
import com.avito.capitalize

public object ContractsTaskNamesBuilder {

    public fun codegenTask(subVariant: String = ""): String {
        return buildName(CodegenTask.NAME) {
            suffixes.add(subVariant)
        }
    }

    public fun validationTask(kind: String, subVariant: String = ""): String {
        return contractsTask("validate", kind, subVariant)
    }

    public fun collectSchemesTask(kind: String, subVariant: String = ""): String {
        return schemesTask("collect", kind, subVariant)
    }

    public fun updateSchemesTask(kind: String, subVariant: String = ""): String {
        return schemesTask("update", kind, subVariant)
    }

    public fun importSchemeTask(kind: String, subVariant: String = ""): String {
        return schemesTask("import", kind, subVariant)
    }

    public fun contractsTask(taskName: String, kind: String, variant: String): String {
        return buildName(taskName) {
            this.kind = kind
            suffixes.add("contracts")
            suffixes.add(variant)
        }
    }

    public fun schemesTask(taskName: String, kind: String, variant: String): String {
        return buildName(taskName) {
            this.kind = kind
            suffixes.add("schemes")
            suffixes.add(variant)
        }
    }

    private fun buildName(
        taskName: String,
        configure: TaskNameBuilder.() -> Unit = {}
    ): String {
        val builder = TaskNameBuilder(taskName)
        builder.configure()
        return builder.build()
    }
}

public class TaskNameBuilder(
    private val taskName: String
) {

    public var kind: String = ""
    public val suffixes: MutableList<String> = mutableListOf()

    public fun build(): String {
        return "${taskName}${kind.capitalizedCamelCase()}${suffixes.format()}"
    }

    private fun List<String>.format(): String {
        return joinToString(separator = "", transform = String::capitalizedCamelCase)
    }
}

internal fun String.capitalizedCamelCase(): String {
    return this.split(' ', '_', '-')
        .joinToString("") { word -> word.capitalize() }
}
