package com.avito.android.owner.adapter

import com.avito.android.model.Owner
import com.avito.android.serializers.OwnerIdSerializer
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * [OwnerAdapter] which serializes [Owner] to a list of ids due [OwnerIdSerializer].
 *
 * Used to upload owner names with warnings, dependencies and other tech budget metrics.
 *
 * Sample output:
 * ```json
 * "owners": ["SpeedId", "MessengerId"]
 * ```
 */
public class OwnerIdAdapter(
    private val ownerSerializer: OwnerIdSerializer
) : OwnerAdapter() {

    @FromJson
    override fun fromJson(reader: JsonReader): Owner? {
        val rawOwner = reader.nextString() ?: return null
        return ownerSerializer.deserialize(rawOwner)
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Owner?) {
        if (value == null) {
            writer.nullValue()
        } else {
            ownerSerializer.serialize(value).forEach(writer::value)
        }
    }
}
