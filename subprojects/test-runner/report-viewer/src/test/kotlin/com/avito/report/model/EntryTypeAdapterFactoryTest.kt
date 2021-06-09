package com.avito.report.model

import com.avito.report.ReportsApiFactory
import com.avito.truth.assertThat
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test

internal class EntryTypeAdapterFactoryTest {

    private val gsonWithEntryAdapter = ReportsApiFactory.gson

    @Test
    fun `serialize - contains placeholder - fileAddress is file to upload`() {
        val entry = Entry.File.createStubInstance(
            fileAddress = FileAddress.File("name.txt")
        )
        val result = gsonWithEntryAdapter.toJson(entry)
        assertThat(result).contains("\"#upload:name.txt\"")
    }

    @Test
    fun `serialize - as it is - fileAddress is url`() {
        val entry = Entry.File.createStubInstance(
            fileAddress = FileAddress.URL("http://stub/name.txt".toHttpUrl())
        )
        val result = gsonWithEntryAdapter.toJson(entry)
        assertThat(result).contains("\"http://stub/name.txt\"")
    }

    @Test
    fun `serialize - error placeholder - fileAddress is error`() {
        val entry = Entry.File.createStubInstance(
            fileAddress = FileAddress.Error(RuntimeException("something went wrong"))
        )
        val result = gsonWithEntryAdapter.toJson(entry)
        assertThat(result).contains("\"#error:something went wrong\"")
    }

    @Test
    fun `deserialize - fileAddress is error - error placeholder`() {
        val json = """{"type":"img_png","timestamp":0,"comment":"","file_address":"#error:something went wrong"}"""
        val result = gsonWithEntryAdapter.fromJson<Entry.File>(json)

        assertThat<FileAddress.Error>(result.fileAddress) {
            assertThat(error.message).contains("something went wrong")
        }
    }

    @Test
    fun `deserialize - fileAddress is url - as it is`() {
        val json = """{"type":"img_png","timestamp":0,"comment":"","file_address":"http://stub/name.txt"}"""
        val result = gsonWithEntryAdapter.fromJson<Entry.File>(json)

        assertThat<FileAddress.URL>(result.fileAddress) {
            assertThat(url.toString()).contains("http://stub/name.txt")
        }
    }

    @Test
    fun `deserialize - fileAddress is file to upload - contains placeholder`() {
        val json = """{"type":"img_png","timestamp":0,"comment":"","file_address":"#upload:name.txt"}"""
        val result = gsonWithEntryAdapter.fromJson<Entry.File>(json)

        assertThat<FileAddress.File>(result.fileAddress) {
            assertThat(fileName).contains("name.txt")
        }
    }

    @Test
    fun `serialize list - contains upload placeholder`() {
        val entryList = listOf(
            Entry.File.createStubInstance(
                fileAddress = FileAddress.File("name.txt")
            )
        )

        val result = gsonWithEntryAdapter.toJson(entryList)
        assertThat(result).apply {
            contains("\"#upload:name.txt\"")
        }
    }

    @Test
    fun `deserialize list - contains upload placeholder`() {
        val json = """[
            |{"type":"img_png","timestamp":0,"comment":"","file_address":"#upload:name.txt"}
            |]""".trimMargin()

        val result = gsonWithEntryAdapter.fromJson<List<Entry>>(json)

        assertThat<List<Entry>>(result) {
            assertThat<Entry.File>(get(0)) {
                assertThat<FileAddress.File>(fileAddress) {
                    assertThat(fileName).contains("name.txt")
                }
            }
        }
    }
}
