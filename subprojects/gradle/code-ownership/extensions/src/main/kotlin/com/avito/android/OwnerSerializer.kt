package com.avito.android

import com.avito.android.model.Owner

@Deprecated("Not used anymore and expended by OwnerFieldSerializer and their implementations")
public interface OwnerSerializer {

    public fun deserialize(rawOwner: String): Owner

    public fun serialize(owner: Owner): String
}
