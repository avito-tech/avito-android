package com.avito.android.tech_budget.internal.owners.adapter

import com.avito.android.OwnerSerializer
import com.avito.android.model.Owner
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal class UploadOwnersAdapter(
    private val ownerSerializer: OwnerSerializer
) : OwnerAdapter() {

    @FromJson
    override fun fromJson(reader: JsonReader): Owner? {
        throw NotImplementedError("Uploading owner does not require json deserialization")
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Owner?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.jsonValue(
                mapOf(
                    "name" to ownerSerializer.serialize(value)
                )
            )
        }
    }
}
