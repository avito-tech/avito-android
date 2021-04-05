package com.avito.report.model

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.lang.reflect.ParameterizedType

class EntryTypeAdapterFactory : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val entryTypeAdapter = EntryTypeAdapter()
        return when {
            type.rawType.isAssignableFrom(List::class.java) ->
                if ((type.type as ParameterizedType).actualTypeArguments[0] == Entry::class.java) {
                    ListEntryTypeAdapter(entryTypeAdapter) as TypeAdapter<T>
                } else {
                    null
                }
            type.rawType.isAssignableFrom(Entry::class.java) -> entryTypeAdapter as TypeAdapter<T>
            else -> null
        }
    }

    private class FileAddressTypeAdapter : TypeAdapter<FileAddress>() {

        private val placeholderPrefix = "#upload:"

        private val errorPrefix = "#error:"

        override fun write(out: JsonWriter, value: FileAddress) {
            when (value) {
                is FileAddress.Error -> out.value("$errorPrefix${value.error.message}")
                is FileAddress.File -> out.value("$placeholderPrefix${value.fileName}")
                is FileAddress.URL -> out.value(value.url.toString())
            }
        }

        override fun read(`in`: JsonReader): FileAddress {
            val raw = `in`.nextString()
            return when {
                raw.startsWith(placeholderPrefix) ->
                    FileAddress.File(fileName = raw.replace(placeholderPrefix, ""))

                raw.startsWith(errorPrefix) ->
                    FileAddress.Error(error = IllegalStateException(raw.replace(errorPrefix, "")))

                else -> try {
                    FileAddress.URL(url = raw.toHttpUrl())
                } catch (exception: IllegalArgumentException) {
                    FileAddress.Error(error = exception)
                }
            }
        }
    }

    private class EntryTypeAdapter : TypeAdapter<Entry>() {

        private val fileAddressTypeAdapter = FileAddressTypeAdapter()

        override fun write(out: JsonWriter, value: Entry) {
            with(out) {
                beginObject()
                name("type")
                value(value.type)
                name("timestamp")
                value(value.timeInSeconds)

                when (value) {
                    is Entry.File -> {
                        name("comment")
                        value(value.comment)
                        name("file_address")
                        fileAddressTypeAdapter.write(out, value.fileAddress)
                    }
                    is Entry.Comment -> {
                        name("title")
                        value(value.title)
                    }
                    is Entry.Field -> {
                        name("comment")
                        value(value.comment)
                        name("value")
                        value(value.value)
                    }
                    is Entry.Check -> {
                        name("title")
                        value(value.title)
                    }
                }
                endObject()
            }
        }

        override fun read(`in`: JsonReader): Entry {
            var type = ""
            var timestamp: Long = 0
            var comment = ""
            var title = ""
            var value = ""
            var fileAddress: FileAddress? = null

            with(`in`) {
                check(peek() == JsonToken.BEGIN_OBJECT) {
                    "Expected ${JsonToken.BEGIN_OBJECT} but was ${peek()} at $this"
                }

                beginObject()
                while (hasNext()) {
                    when (nextName()) {
                        "type" -> type = nextString()
                        "timestamp" -> timestamp = nextLong()
                        "comment" -> comment = nextString()
                        "title" -> title = nextString()
                        "value" -> value = nextString()
                        "file_address" -> fileAddress = fileAddressTypeAdapter.read(`in`)
                    }
                }
                endObject()
            }
            return when (type) {
                "comment" -> Entry.Comment(title = title, timeInSeconds = timestamp)
                "field" -> Entry.Field(comment = comment, value = value, timeInSeconds = timestamp)
                "check" -> Entry.Check(title = title, timeInSeconds = timestamp)
                "html" -> Entry.File(
                    comment = comment,
                    fileAddress = requireNotNull(fileAddress),
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.html
                )
                "img_png" -> Entry.File(
                    comment = comment,
                    fileAddress = requireNotNull(fileAddress),
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.img_png
                )
                "video" -> Entry.File(
                    comment = comment,
                    fileAddress = requireNotNull(fileAddress),
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.video
                )
                "plain_text" -> Entry.File(
                    comment = comment,
                    fileAddress = requireNotNull(fileAddress),
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.plain_text
                )
                else -> error("unsupported type: $type")
            }
        }
    }

    private class ListEntryTypeAdapter(private val entryTypeAdapter: EntryTypeAdapter) : TypeAdapter<List<Entry>>() {

        override fun write(out: JsonWriter, value: List<Entry>) {
            with(out) {
                beginArray()
                value.forEach {
                    entryTypeAdapter.write(out, it)
                }
                endArray()
            }
        }

        override fun read(`in`: JsonReader): List<Entry> {
            val result = mutableListOf<Entry>()
            with(`in`) {
                check(peek() == JsonToken.BEGIN_ARRAY) {
                    "Expected ${JsonToken.BEGIN_ARRAY} but was ${peek()} at $this"
                }

                beginArray()
                while (hasNext()) {
                    result.add(entryTypeAdapter.read(this))
                }
                endArray()
            }
            return result
        }
    }
}
