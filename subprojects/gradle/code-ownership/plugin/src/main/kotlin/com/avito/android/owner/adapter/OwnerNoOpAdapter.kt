package com.avito.android.owner.adapter

import com.avito.android.model.Owner
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

internal class OwnerNoOpAdapter : OwnerAdapter() {

    @FromJson
    override fun fromJson(reader: JsonReader): Owner? {
        error("Meet Owner type that cannot be read from json. Please use another adapter.")
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Owner?) {
        error("Meet Owner type that cannot be write to json. Please use another adapter.")
    }
}
