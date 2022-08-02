package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class NupokatiProject(public val id: String) {

    @SerialName("avito")
    AVITO("avito"),

    @SerialName("avito_test")
    AVITO_TEST("avito_test")
}
