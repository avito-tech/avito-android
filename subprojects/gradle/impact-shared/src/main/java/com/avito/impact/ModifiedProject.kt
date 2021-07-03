package com.avito.impact

import com.avito.impact.changes.ChangedFile
import org.gradle.api.Project

public class ModifiedProject(
    public val project: Project,
    public val changedFiles: List<ChangedFile>
)
