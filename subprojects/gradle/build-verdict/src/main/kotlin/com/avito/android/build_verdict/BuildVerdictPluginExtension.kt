package com.avito.android.build_verdict

import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout

abstract class BuildVerdictPluginExtension(
    projectLayout: ProjectLayout
) {
    val buildVerdictOutputDir: Directory = projectLayout.projectDirectory.dir("outputs/build-verdict")
}
