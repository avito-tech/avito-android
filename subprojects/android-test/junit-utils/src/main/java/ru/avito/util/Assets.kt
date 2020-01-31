package ru.avito.util

import java.io.FileNotFoundException
import java.io.IOException

object Assets {

    @Throws(IOException::class)
    fun readAsset(path: String): String {
        val inputStream = Assets::class.java.classLoader.getResourceAsStream(path)
                ?: throw FileNotFoundException("File [$path] not found.")
        return inputStream.bufferedReader().readText()
    }
}