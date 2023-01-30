package com.avito.emcee.worker

import com.avito.emcee.moshi.SecondsToDurationAdapter
import com.avito.emcee.worker.model.dequeued
import com.avito.emcee.worker.model.noBucket
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class GetBucketResponseDeserializationTest {

    @OptIn(ExperimentalStdlibApi::class)
    private val moshi = Moshi.Builder()
        .addAdapter(SecondsToDurationAdapter())
        .build()

    @OptIn(ExperimentalStdlibApi::class)
    private val adapter: JsonAdapter<GetBucketResponse> = moshi.adapter()

    @Test
    fun `get bucket - parsed correctly - when is dequeued`() {
        val dequeuedJson = File(javaClass.classLoader.getResource("get_bucket_dequeued.json")!!.file).readText()
        val result = adapter.fromJson(dequeuedJson)
        assertEquals(GetBucketResponse.dequeued(), result)
    }

    @Test
    fun `get bucket - parsed correctly - when there is no bucket`() {
        val checkAgainJson =
            File(javaClass.classLoader.getResource("get_bucket_check_again_later.json")!!.file).readText()
        val result = adapter.fromJson(checkAgainJson)
        assertEquals(GetBucketResponse.noBucket(), result)
    }
}
