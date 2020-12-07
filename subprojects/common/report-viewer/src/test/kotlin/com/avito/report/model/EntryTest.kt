package com.avito.report.model

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.typedToJson
import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

internal class EntryTest {

    @Test
    fun `serialize deserialize test`() {
        val entryList = mutableListOf(
            Entry.Comment("comment", 12345),
            Entry.Check("check", 1234),
            Entry.Field("field", "value", 123),
            Entry.File("file", "/file.png", 12, Entry.File.Type.img_png)
        )

        """[
  {
    "type": "comment",
    "timestamp": 12345,
    "title": "comment"
  },
  {
    "type": "check",
    "timestamp": 1234,
    "title": "check"
  },
  {
    "type": "field",
    "timestamp": 123,
    "comment": "field",
    "value": "value"
  },
  {
    "type": "img_png",
    "timestamp": 12,
    "comment": "file"
  }
]
        """.trimIndent()

        val gson = GsonBuilder()
            .registerTypeAdapterFactory(EntryTypeAdapterFactory())
            .create()

        val json = gson.typedToJson(entryList)
        val result = gson.fromJson<List<Entry>>(json)
        assertThat(result).isEqualTo(entryList)
    }
}
