package com.avito.android

internal const val CD_TASK_GROUP: String = "cd"

internal const val DEFAULT_RELEASE_VARIANT: String = "release"

internal fun uploadCdBuildResultTaskName(variantSlug: String): String = "uploadCdBuildResult$variantSlug"
