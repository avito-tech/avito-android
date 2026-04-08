package com.avito.android.string_transform.internal.task.signing

import com.avito.android.signer.AbstractSignTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

internal abstract class SignTransformedApkTask @Inject constructor(
    workerExecutor: WorkerExecutor,
    objects: ObjectFactory,
) : AbstractSignTask(workerExecutor, objects) {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val unsignedApkFile: RegularFileProperty

    override fun unsignedFile(): File {
        return unsignedApkFile.get().asFile
    }

    override fun signedFilenameTransformer(unsignedFileName: String): String {
        return unsignedFileName.replace("-unsigned", "")
    }
}
