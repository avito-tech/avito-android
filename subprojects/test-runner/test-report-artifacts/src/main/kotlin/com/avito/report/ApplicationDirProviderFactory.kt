package com.avito.report

import java.io.File

public object ApplicationDirProviderFactory {

    @Suppress("SdCardPath")
    public fun create(
        api: Int,
        appPackage: String
    ): ApplicationDirProvider {
        val dataPath = if (api >= 30) {
            "/storage/emulated/0/Android/media/$appPackage"
        } else {
            "/sdcard/Android/data/$appPackage/files"
        }
        return object : ApplicationDirProvider {
            override val dir: File = File(dataPath)
        }
    }
}
