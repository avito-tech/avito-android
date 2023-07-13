package com.avito.android.model

import com.avito.android.OwnerNameSerializer

class FakeOwnersSerializer : OwnerNameSerializer {

    override fun deserialize(ownerName: String): Owner {
        return FakeOwners.valueOf(ownerName)
    }

    override fun serialize(owner: Owner): String {
        return (owner as FakeOwners).name
    }
}
