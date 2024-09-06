package com.avito.android.providers

import com.avito.android.diff.provider.OwnersProvider
import com.avito.android.model.Owner
import com.avito.android.model.network.AvitoOwnersClient

internal class RemoteAvitoModuleOwnersProvider(
    private val client: AvitoOwnersClient,
) : OwnersProvider {

    override fun get(): Set<Owner> {
        return client.getAvitoOwners()
            .flatMap { owner ->
                mutableListOf<RemoteAvitoCodeOwner>().apply {
                    add(
                        RemoteAvitoCodeOwner(
                            name = owner.name,
                            id = owner.id,
                            type = owner.type,
                            parent = null
                        )
                    )
                    addAll(owner.children.map { child ->
                        RemoteAvitoCodeOwner(
                            name = child.name,
                            id = child.id,
                            type = child.type,
                            parent = owner.name
                        )
                    })
                }
            }
            .toSet()
    }
}
