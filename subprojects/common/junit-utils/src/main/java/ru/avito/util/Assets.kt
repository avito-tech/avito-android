package ru.avito.util

import java.io.FileNotFoundException
import java.io.IOException

public object Assets {

    // todo merge with :resources module
    @Throws(IOException::class)
    public fun readAsset(path: String): String {
        val inputStream = Assets::class.java.classLoader.getResourceAsStream(path)
            ?: throw FileNotFoundException("File [$path] not found.")
        return inputStream.bufferedReader().readText()
    }
}
