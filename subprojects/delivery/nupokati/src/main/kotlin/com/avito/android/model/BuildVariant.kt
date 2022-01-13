package com.avito.android.model

import com.google.gson.annotations.SerializedName

public enum class BuildVariant {

    @SerializedName("release")
    RELEASE,

    @SerializedName("staging")
    STAGING,

    @SerializedName("debug")
    DEBUG
}
