package com.avito.emcee.worker

import com.avito.emcee.worker.model.dequeued
import com.avito.emcee.worker.model.noBucket
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class GetBucketResponseDeserializationTest {

    private val moshi = Moshi.Builder().build()

    // TODO: Provide generated adapters for tests too.
    private val adapter: JsonAdapter<GetBucketResponse> = moshi.adapter(GetBucketResponse::class.java)

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
