package com.avito.teamcity.saturate

import org.jetbrains.teamcity.rest.Build

internal data class ExtendedBuildInfo<T : Any>(
    val build: Build,
    val data: T,
) : Build by build
