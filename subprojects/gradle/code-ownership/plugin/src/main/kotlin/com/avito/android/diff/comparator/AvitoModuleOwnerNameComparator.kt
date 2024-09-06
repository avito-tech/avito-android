package com.avito.android.diff.comparator

import com.avito.android.model.AvitoCodeOwner
import com.avito.android.model.Owner
import com.avito.android.providers.RemoteAvitoCodeOwner

internal class AvitoModuleOwnerNameComparator : OwnersComparator {

    override fun isSame(expected: Owner, actual: Owner): Boolean {
        val expectedName = getOwnerIdentity(expected)
        val actualName = getOwnerIdentity(actual)
        return expectedName.equals(actualName, ignoreCase = true)
    }

    private fun getOwnerIdentity(owner: Owner): String = when (owner) {
        is AvitoCodeOwner -> owner.type.id
        is RemoteAvitoCodeOwner -> owner.id
        else -> error("Owners at avito repository must be created via AvitoCodeOwner or RemoteAvitoModuleOwner")
    }
}
