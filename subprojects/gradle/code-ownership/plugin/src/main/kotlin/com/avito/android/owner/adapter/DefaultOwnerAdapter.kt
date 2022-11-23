package com.avito.android.owner.adapter

import com.avito.android.OwnerSerializer
import com.avito.android.model.Owner
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import javax.inject.Provider

/**
 * [OwnerAdapter] which serializes [Owner] to a plain String with [OwnerSerializer].
 *
 * Used to upload owner names with warnings, dependencies and other tech budget metrics.
 *
 * Sample output:
 * ```json
 * "owners": ["Speed", "Messenger"]
 * ```
 */
public class DefaultOwnerAdapter(
    private val ownerSerializer: Provider<OwnerSerializer>
) : OwnerAdapter() {

    public constructor(ownerSerializer: OwnerSerializer) : this(Provider { ownerSerializer })

    @FromJson
    override fun fromJson(reader: JsonReader): Owner? {
        val rawOwner = reader.nextString() ?: return null
        return ownerSerializer.get().deserialize(rawOwner)
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Owner?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(ownerSerializer.get().serialize(value))
        }
    }
}
