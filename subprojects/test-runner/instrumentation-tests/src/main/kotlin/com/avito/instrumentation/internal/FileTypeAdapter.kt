package com.avito.instrumentation.internal

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File

/**
 * Manually serialize to avoid exception on later java runtimes
 *
 * Caused by: java.lang.reflect.InaccessibleObjectException:
 * Unable to make field private final java.lang.String java.io.File.path accessible:
 * module java.base does not "opens java.io" to unnamed module...
 */
internal class FileTypeAdapter : TypeAdapter<File>() {

    override fun write(writer: JsonWriter, file: File) {
        writer.value(file.canonicalPath)
    }

    override fun read(reader: JsonReader): File {
        return File(reader.nextString())
    }
}
