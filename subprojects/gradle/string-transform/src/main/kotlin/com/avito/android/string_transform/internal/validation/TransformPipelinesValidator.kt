package com.avito.android.string_transform.internal.validation

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.string_transform.TransformPipelineSpec

internal object TransformPipelinesValidator {

    fun validate(
        projectPath: String,
        pipeline: TransformPipelineSpec,
    ) {
        if (!pipeline.variant.isPresent) {
            throw Problem.Builder(
                shortDescription = "String-transform pipeline '${pipeline.name}' does not declare variant",
                context = pipelineContext(projectPath, pipeline)
            )
                .because("Plugin binds pipelines only to exact Android variant names.")
                .addSolution("Add variant(\"release\") or another exact variant name to this pipeline.")
                .build()
                .asRuntimeException()
        }

        if (pipeline.rules.totalRuleCount == 0) {
            throw Problem.Builder(
                shortDescription = "String-transform pipeline '${pipeline.name}' does not declare any rules",
                context = pipelineContext(projectPath, pipeline)
            )
                .because("A pipeline without rules does nothing.")
                .addSolution("Add at least one exact(...) or caseExpanded(...) rule.")
                .build()
                .asRuntimeException()
        }
    }

    private fun pipelineContext(
        projectPath: String,
        pipeline: TransformPipelineSpec,
    ): String = "Configuring transformStrings.create(\"${pipeline.name}\") in $projectPath"
}
