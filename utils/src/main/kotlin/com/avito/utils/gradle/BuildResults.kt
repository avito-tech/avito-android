package com.avito.utils.gradle

import org.gradle.BuildResult

val BuildResult.isBuildAction
    get() = action == "Build"
