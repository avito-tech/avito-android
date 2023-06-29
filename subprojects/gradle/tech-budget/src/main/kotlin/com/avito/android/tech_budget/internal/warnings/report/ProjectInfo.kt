package com.avito.android.tech_budget.internal.warnings.report

import com.avito.android.model.Owner
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.squareup.moshi.JsonClass
import org.gradle.api.Project

@JsonClass(generateAdapter = true)
internal class ProjectInfo(
    val path: String,
    val owners: Collection<Owner>,
) {

    companion object {

        fun fromProject(project: Project): ProjectInfo {
            return ProjectInfo(
                path = project.path,
                owners = project.requireCodeOwnershipExtension().owners.getOrElse(emptySet())
            )
        }
    }
}
