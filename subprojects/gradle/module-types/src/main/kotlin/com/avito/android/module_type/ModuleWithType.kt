package com.avito.android.module_type

import java.io.Serializable

public data class ModuleWithType(
    val path: String,
    val type: ModuleType
) : Serializable
