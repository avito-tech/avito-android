package com.avito.module.metrics

import com.avito.android.CodeOwnershipExtension
import com.avito.android.model.Owner
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType

public abstract class CollectModuleBetweennessCentralityTask : DefaultTask() {

    init {
        description = "Collect betweenness centrality metric for every module"
    }

    @get:OutputFile
    public abstract val output: RegularFileProperty

    private val Project.owners: Set<Owner>
        get() = this.extensions.findByType<CodeOwnershipExtension>()?.owners?.get().orEmpty()

    @TaskAction
    public fun action() {
        val betweennessCentrality = CollectModuleBetweennessCentralityAction().collect(project)

        CsvMapper().writer().writeValues(output.asFile.get()).use { writer ->
            writer.write(arrayOf("module", "betweenness-centrality", "owners"))

            betweennessCentrality
                .toList()
                .sortedByDescending { it.second }
                .forEach { (project, betweennessCentrality) ->
                    writer.write(arrayOf(
                        project.path,
                        betweennessCentrality.toString(),
                        project.owners.joinToString(", "),
                    ))
                }
        }
    }
}
