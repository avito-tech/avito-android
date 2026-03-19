package com.avito.android.string_transform.internal.configuration

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.string_transform.TransformPipelineSpec
import com.avito.android.string_transform.internal.rules.DeclaredRule.CaseExpanded

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

        val declaredRules = pipeline.rules.declaredRules.get()

        if (declaredRules.isEmpty()) {
            throw Problem.Builder(
                shortDescription = "String-transform pipeline '${pipeline.name}' does not declare any rules",
                context = pipelineContext(projectPath, pipeline)
            )
                .because("A pipeline without rules does nothing.")
                .addSolution("Add at least one exact(...) or caseExpanded(...) rule.")
                .build()
                .asRuntimeException()
        }

        val hasEmptyGeneratedForms = declaredRules.filterIsInstance<CaseExpanded>().any { it.generatedForms.isEmpty() }
        if (hasEmptyGeneratedForms) {
            throw Problem.Builder(
                shortDescription = "String-transform pipeline '${pipeline.name}' declares " +
                    "caseExpanded rule without generated forms",
                context = pipelineContext(projectPath, pipeline)
            )
                .because("Generated-form selection is part of caseExpanded declaration contract.")
                .addSolution("Pass one or more forms like GeneratedForm.LOWER to caseExpanded(...).")
                .build()
                .asRuntimeException()
        }
    }

    private fun pipelineContext(
        projectPath: String,
        pipeline: TransformPipelineSpec,
    ): String = "Configuring transformStrings.create(\"${pipeline.name}\") in $projectPath"
}
