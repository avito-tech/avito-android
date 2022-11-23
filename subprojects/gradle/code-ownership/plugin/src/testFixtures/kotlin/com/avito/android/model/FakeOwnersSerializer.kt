package com.avito.android.model

import com.avito.android.OwnerSerializer

class FakeOwnersSerializer : OwnerSerializer {
    override fun deserialize(rawOwner: String): Owner {
        return FakeOwners.valueOf(rawOwner)
    }

    override fun serialize(owner: Owner): String {
        return (owner as FakeOwners).name
    }
}
