package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class AndroidArtifactType {

    @SerialName("bundle")
    BUNDLE,

    @SerialName("apk")
    APK
}
