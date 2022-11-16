package com.avito.android.tech_budget.internal.owners

import com.avito.android.OwnerSerializer
import com.avito.android.model.Owner
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.owners.adapter.UploadOwnersAdapter
import com.avito.android.tech_budget.internal.owners.models.UploadOwnersRequestBody
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

internal abstract class UploadOwnersTask : DefaultTask() {

    @get:Input
    abstract val owners: SetProperty<Owner>

    @get:Input
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @TaskAction
    fun uploadOwners() {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = UploadOwnersApi.create(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            uploadOwnersAdapter = UploadOwnersAdapter(ownerSerializer.get())
        )
        service.dumpOwners(UploadOwnersRequestBody(DumpInfo.fromExtension(dumpInfoConfig), owners.get()))
            .executeWithHttpFailure(errorMessage = "Upload owners request failed")
    }

    companion object {
        const val NAME = "uploadOwners"
    }
}
