package com.avito.android.info

import com.avito.android.CodeOwnershipExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import java.io.File

public abstract class ReportCodeOwnershipInfoTask : DefaultTask() {

    @TaskAction
    public fun printOwnership() {
        val file = File(project.projectDir.toString(), "ownership.csv").apply {
            if (exists()) delete()
            createNewFile()
        }

        project.subprojects { subproject ->
            file.appendText(subproject.formatToCsvLine())
        }
    }

    private fun Project.formatToCsvLine(): String {
        val ownersCell = extensions.getByType<CodeOwnershipExtension>().owners.joinToString(
            separator = ",",
            prefix = "\"",
            postfix = "\"",
            transform = { it.toString() }
        )
        return "$path,$ownersCell\n"
    }
}
