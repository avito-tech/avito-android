package com.avito.android.signer

import com.avito.android.getApkOrThrow
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class SignApkTask @Inject constructor(
    workerExecutor: WorkerExecutor,
    objects: ObjectFactory
) : AbstractSignTask(workerExecutor, objects) {

    @get:InputDirectory
    public abstract val apkDirectory: DirectoryProperty

    override fun unsignedFile(): File {
        return apkDirectory.get().getApkOrThrow()
    }

    override fun signedFilenameTransformer(unsignedFileName: String): String {
        return unsignedFileName.replace("-unsigned", "")
    }
}
