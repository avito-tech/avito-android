package com.avito.report.model

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
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

    private class EntryTypeAdapter : TypeAdapter<Entry>() {

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
                        value(value.fileAddress)
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
            var fileAddress = ""

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
                        "file_address" -> fileAddress = nextString()
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
                    fileAddress = fileAddress,
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.html
                )
                "img_png" -> Entry.File(
                    comment = comment,
                    fileAddress = fileAddress,
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.img_png
                )
                "video" -> Entry.File(
                    comment = comment,
                    fileAddress = fileAddress,
                    timeInSeconds = timestamp,
                    fileType = Entry.File.Type.video
                )
                "plain_text" -> Entry.File(
                    comment = comment,
                    fileAddress = fileAddress,
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
