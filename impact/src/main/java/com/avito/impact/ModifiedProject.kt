package com.avito.impact

import com.avito.impact.changes.ChangedFile
import org.gradle.api.Project

class ModifiedProject(
    val project: Project,
    val changedFiles: List<ChangedFile>
)
