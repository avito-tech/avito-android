package com.avito.emcee.worker.internal.config

import com.avito.emcee.worker.Config
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.File

@ExperimentalStdlibApi
internal class ConfigReader(
    moshi: Moshi
) {
    private val configAdapter = moshi.adapter<Config>()

    fun read(configFile: File): Config {
        require(configFile.exists()) {
            "Can't find config $configFile"
        }
        val configJsonRaw = configFile.readText()
        return requireNotNull(
            configAdapter.fromJson(configJsonRaw)
        )
    }
}
