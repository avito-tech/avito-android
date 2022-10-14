package com.avito.android.info

import com.avito.android.ownership
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class ReportCodeOwnershipInfoTask : DefaultTask() {

    @get:OutputFile
    public abstract val outputCsv: RegularFileProperty

    @TaskAction
    public fun printOwnership() {
        val file = outputCsv.get().asFile.apply {
            writeText("name,owners\n")
        }

        project.subprojects { subproject ->
            file.appendText(subproject.formatToCsvLine())
        }
    }

    private fun Project.formatToCsvLine(): String {
        val ownersCell = extensions.ownership.owners.joinToString(
            separator = ",",
            prefix = "\"",
            postfix = "\"",
            transform = { it.toString() }
        )
        return "$path,$ownersCell\n"
    }
}
