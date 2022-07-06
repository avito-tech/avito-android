package com.avito.cd

import com.google.gson.annotations.SerializedName

@Deprecated("Use model.BuildVariant")
public enum class BuildVariant {

    @SerializedName("release")
    RELEASE,

    @SerializedName("staging")
    STAGING,

    @SerializedName("debug")
    DEBUG
}
