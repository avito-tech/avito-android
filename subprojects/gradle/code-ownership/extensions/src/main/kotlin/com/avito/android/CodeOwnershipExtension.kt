package com.avito.android

import com.avito.android.model.Owner
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property

public abstract class CodeOwnershipExtension(
    objects: ObjectFactory,
) {

    public val owners: SetProperty<Owner> = objects.setProperty(Owner::class.java)
        .convention(emptySet())
    public val emptyOwnersErrorMessage: Property<String> =
        objects.property<String>().convention(DEFAULT_EMPTY_OWNERS_ERROR_MESSAGE)

    public fun owners(vararg owners: Owner) {
        this.owners.set(owners.toSet())
    }

    public fun emptyOwnersErrorMessage(message: String) {
        this.emptyOwnersErrorMessage.set(message)
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
