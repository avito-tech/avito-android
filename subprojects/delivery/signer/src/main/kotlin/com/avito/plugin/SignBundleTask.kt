package com.avito.plugin

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

public abstract class SignBundleTask @Inject constructor(
    workerExecutor: WorkerExecutor
) : AbstractSignTask(workerExecutor) {

    @get:InputFile
    public abstract val bundleFile: Property<RegularFile>

    override fun unsignedFile(): File = bundleFile.get().asFile
}
