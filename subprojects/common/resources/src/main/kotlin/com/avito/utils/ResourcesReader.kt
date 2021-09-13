package com.avito.utils

import java.io.File
import java.io.FileNotFoundException
import java.net.URL

public object ResourcesReader {

    private fun read(path: String): URL {
        val url = ResourcesReader::class.java.classLoader.getResource(path)
        requireNotNull(url) { "Resource from $path not found" }
        return url
    }

    /**
     *  @param path relative to packed resource;
     *      for example `src/test/resources/some-file.txt` should be referenced as `some-file.txt`
     *      and `src/test/resources/dir/another-file.txt` as `dir/another-file.txt`
     */
    public fun readText(path: String): String {
        val inputStream = ResourcesReader::class.java.classLoader.getResourceAsStream(path)
            ?: throw FileNotFoundException("File [$path] not found.")
        return inputStream.bufferedReader().readText()
    }

    /**
     *  @param path relative to packed resource;
     *      for example `src/test/resources/some-file.txt` should be referenced as `some-file.txt`
     *      and `src/test/resources/dir/another-file.txt` as `dir/another-file.txt`
     */
    public fun readFile(path: String): File = File(read(path).file)
}
