package com.avito.report

import java.io.File

public object ApplicationDirProviderFactory {

    @Suppress("SdCardPath")
    public fun create(
        api: Int,
        packageName: String
    ): ApplicationDirProvider {
        val dataPath = if (api >= 30) {
            "/storage/emulated/0/Android/media/$packageName"
        } else {
            "/sdcard/Android/data/$packageName/files"
        }
        return object : ApplicationDirProvider {
            override val dir: File = File(dataPath)
        }
    }
}
