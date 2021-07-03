package com.avito.utils

import java.io.File
import java.util.Properties
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public fun File.createOrClear() {
    if (exists()) {
        writer().use { it.write("") }
    } else {
        parentFile.mkdirs()
        createNewFile()
    }
}

public fun File.rewriteNewLineList(listOfData: Iterable<String>) {
    rewriteIterable(listOfData) { it.joinToString("\n") }
}

public fun <K, V> File.rewriteNewLineMap(data: Map<K, V>) {
    rewriteIterable(data) {
        it.map { (key, value) -> "$key -> $value" }
            .joinToString(separator = "\n")
    }
}

public fun <T> File.rewriteIterable(data: T, transform: (T) -> String) {
    createOrClear()
    writer(Charsets.UTF_8).use { it.write(transform.invoke(data)) }
}

@OptIn(ExperimentalContracts::class)
public fun File?.hasFileContent(): Boolean {
    contract {
        returns(true) implies (this@hasFileContent != null)
    }

    if (this == null) return false
    return this.exists() && this.isFile && this.length() > 0
}

public fun File.loadProperties(): Properties {
    val properties = Properties()
    this.bufferedReader().use { reader ->
        properties.load(reader)
    }
    return properties
}

public fun loadProperties(path: String): Properties = File(path).loadProperties()
