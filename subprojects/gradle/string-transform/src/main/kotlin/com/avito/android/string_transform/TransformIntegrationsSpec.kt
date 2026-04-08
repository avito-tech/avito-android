package com.avito.android.string_transform

import org.gradle.api.Action
import org.gradle.api.tasks.Nested
import javax.inject.Inject

public abstract class TransformIntegrationsSpec @Inject constructor() {

    @get:Nested
    public abstract val signing: TransformSigningSpec

    public fun signing(action: Action<in TransformSigningSpec>) {
        action.execute(signing)
    }
}
