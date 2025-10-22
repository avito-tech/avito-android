package com.avito.teamcity.saturate

import org.jetbrains.teamcity.rest.Build

internal fun interface BuildInfoSaturator {

    fun saturate(build: Build): Build
}

internal fun Build.saturateWith(saturator: BuildInfoSaturator): Build {
    return saturator.saturate(this)
}
