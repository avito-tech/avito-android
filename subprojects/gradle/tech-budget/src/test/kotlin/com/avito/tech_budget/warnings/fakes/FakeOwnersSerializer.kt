package com.avito.tech_budget.warnings.fakes

import com.avito.android.OwnerSerializer
import com.avito.android.model.Owner

internal class FakeOwnersSerializer : OwnerSerializer {
    override fun deserialize(rawOwner: String): Owner {
        return FakeOwners.valueOf(rawOwner)
    }

    override fun serialize(owner: Owner): String {
        return (owner as FakeOwners).name
    }
}
