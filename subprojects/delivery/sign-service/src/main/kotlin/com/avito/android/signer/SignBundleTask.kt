package com.avito.android.signer

import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class SignBundleTask @Inject constructor(
    workerExecutor: WorkerExecutor,
    objects: ObjectFactory
) : AbstractSignTask(workerExecutor, objects) {

    @get:InputFile
    public val bundleFile: Property<RegularFile> = objects.fileProperty()

    override fun unsignedFile(): File {
        return bundleFile.get().asFile
    }

    /**
     * already named app-variant.aab
     */
    override fun signedFilenameTransformer(unsignedFileName: String): String {
        return unsignedFileName
    }
}
