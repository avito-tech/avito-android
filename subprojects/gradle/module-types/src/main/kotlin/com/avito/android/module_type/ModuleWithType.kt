package com.avito.android.module_type

import java.io.Serializable

public data class ModuleWithType(
    val path: String,
    val type: ModuleType? // TODO: make non-null in MBS-12266
) : Serializable
