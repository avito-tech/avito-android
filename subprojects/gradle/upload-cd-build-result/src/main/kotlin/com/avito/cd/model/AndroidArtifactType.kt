package com.avito.cd.model

import com.google.gson.annotations.SerializedName

public enum class AndroidArtifactType {
    @SerializedName("bundle")
    BUNDLE,

    @SerializedName("apk")
    APK
}
