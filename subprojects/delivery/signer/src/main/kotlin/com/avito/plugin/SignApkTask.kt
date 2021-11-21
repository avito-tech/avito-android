package com.avito.plugin

import com.avito.android.getApkOrThrow
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class SignApkTask @Inject constructor(
    workerExecutor: WorkerExecutor
) : AbstractSignTask(workerExecutor) {

    @get:InputDirectory
    public abstract val apkDirectory: DirectoryProperty

    override fun unsignedFile(): File = apkDirectory.get().getApkOrThrow()
}
