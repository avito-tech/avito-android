package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class AndroidArtifactType {

    @SerialName("bundle")
    BUNDLE,

    @SerialName("apk")
    APK
}
