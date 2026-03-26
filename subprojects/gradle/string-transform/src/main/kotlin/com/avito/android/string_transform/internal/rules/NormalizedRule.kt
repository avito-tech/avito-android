package com.avito.android.string_transform.internal.rules

import java.io.Serializable

internal data class NormalizedRule(
    val from: String,
    val to: String,
) : Serializable
