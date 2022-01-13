package com.avito.android.model

import com.google.gson.annotations.SerializedName

public enum class NupokatiProject(public val id: String) {

    @SerializedName("avito")
    AVITO("avito"),

    @SerializedName("avito_test")
    AVITO_TEST("avito_test")
}
