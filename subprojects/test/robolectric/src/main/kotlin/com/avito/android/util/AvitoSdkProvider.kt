package com.avito.android.util

import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.SdkProvider
import javax.annotation.Priority

@Priority(Integer.MAX_VALUE)
class AvitoSdkProvider : SdkProvider {

    private val availableSdks = setOf(
        RobolectricSdk(apiLevel = 23, androidVersion = "6.0.1", androidRevision = "r3", robolectricRevision = "r1"),
        RobolectricSdk(apiLevel = 29, androidVersion = "10", androidRevision = "", robolectricRevision = "5803371")
    )

    override fun getSdks(): Collection<Sdk> = availableSdks
}
