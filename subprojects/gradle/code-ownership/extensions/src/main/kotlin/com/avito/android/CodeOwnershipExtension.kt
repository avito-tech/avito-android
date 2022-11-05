package com.avito.android

import com.avito.android.model.Owner

public abstract class CodeOwnershipExtension {

    // TODO MA-2868 Replace ordinary var properties with gradle way properties
    public var owners: Set<Owner> = emptySet()
    public var emptyOwnersErrorMessage: String = DEFAULT_EMPTY_OWNERS_ERROR_MESSAGE

    public fun owners(vararg owners: Owner) {
        this.owners = owners.toSet()
    }

    public fun emptyOwnersErrorMessage(message: String) {
        this.emptyOwnersErrorMessage = message
    }

    private companion object {
        val DEFAULT_EMPTY_OWNERS_ERROR_MESSAGE =
            """
                |Owners must be set for the %s project.
                |Configure the ownership extension for this project in the buildscript: 
                |
                |ownership {
                |   owners(Owner1, Owner2)
                |}
            """.trimMargin()
    }
}
