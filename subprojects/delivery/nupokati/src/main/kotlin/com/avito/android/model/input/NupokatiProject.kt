package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class NupokatiProject(val id: String) {

    @SerialName("avito")
    AVITO("avito"),

    @SerialName("avito_test")
    AVITO_TEST("avito_test")
}
