package com.avito.android.build_checks.internal.unique_app_res

import com.android.resources.ResourceType

public data class Resource(
    val type: ResourceType,
    val name: String
) : java.io.Serializable
