package com.avito.android.tech_budget.internal.owners.adapter

import com.avito.android.OwnerSerializer
import com.avito.android.model.Owner
import com.avito.android.owner.adapter.OwnerAdapter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * [OwnerAdapter] which serializes [Owner] to a plain String with [OwnerSerializer].
 *
 * Used to upload owners through `dumpOwners` method. Contains all necessary information about an owner.
 * Uses [OwnerSerializer] to serialize owner's name.
 *
 * Sample output:
 * ```json
 * "owners": [
 *      {
 *          "name": "Speed"
 *      },
 *      {
 *          "name": "Messenger"
 *      }
 * ]
 * ```
 */
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
