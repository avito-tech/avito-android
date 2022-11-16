package com.avito.android

import com.avito.android.model.Owner

public interface OwnerSerializer {

    public fun deserialize(rawOwner: String): Owner

    public fun serialize(owner: Owner): String
}
