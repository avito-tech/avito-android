package com.avito.android.providers

import com.avito.android.model.Owner
import com.avito.android.model.network.OwnerType
import com.avito.capitalize

internal class RemoteAvitoCodeOwner(
    val type: OwnerType,
    val name: String,
    val id: String,
    val parent: String?
) : Owner {
    override fun toString() =
        "${type.key.capitalize()} ${name}${if (parent != null) " (Юнит $parent)" else ""}"
}
