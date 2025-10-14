package com.avito.android.contracts.platform.extension.configurations.fixation

import com.avito.android.contracts.platform.scheme.fixation.UpsertService
import org.gradle.api.Named
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class FixationConfiguration @Inject constructor() : Named {

    public abstract val upsertService: Property<UpsertService<*>>
}
