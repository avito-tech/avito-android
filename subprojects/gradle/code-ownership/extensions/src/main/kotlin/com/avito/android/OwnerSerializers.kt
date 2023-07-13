package com.avito.android

import com.avito.android.model.Owner

public sealed interface OwnerSerializer

/**
 * Serializer that can serializer Owner to plain string by his name.
 *
 * Serialization and deserialization performs by only name of owner.
 */
public interface OwnerNameSerializer : OwnerSerializer {

    public fun deserialize(ownerName: String): Owner

    public fun serialize(owner: Owner): String
}

/**
 * A serializer that can serialize an owner into a regular string by its identifier.
 *
 * The serialization operation may contain a list of identifiers,
 * and the identifiers may be both own and child identifiers,
 * such as unit command identifiers.
 *
 * The deserialization operation must identify the Owner by an unambiguous identifier.
 */
public interface OwnerIdSerializer : OwnerSerializer {

    public fun deserialize(ownerId: String): Owner

    public fun serialize(owner: Owner): List<String>
}

public object OwnerNoOpSerializer : OwnerSerializer
