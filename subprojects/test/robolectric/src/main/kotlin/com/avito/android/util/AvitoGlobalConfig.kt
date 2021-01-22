package com.avito.android.util

import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.GlobalConfigProvider
import javax.annotation.Priority

@Priority(Integer.MAX_VALUE)
class AvitoGlobalConfig : GlobalConfigProvider {

    override fun get(): Config =
        Config.Builder
            .defaults()
            .setSdk(29)
            .build()
}
