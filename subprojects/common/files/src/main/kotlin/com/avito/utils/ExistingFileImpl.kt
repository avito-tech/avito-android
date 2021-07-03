package com.avito.utils

import java.io.File

public class ExistingFileImpl(file: File) : ExistingFile {

    override val file: File = file
        get() {
            require(field.exists()) { "${field.path} must exists" }
            require(field.isFile) { "${field.path} must be a file" }
            return field
        }

    public constructor(directory: ExistingDirectory, fileName: String) : this(
        File(
            directory.dir,
            fileName
        )
    )

    override fun toString(): String = file.toString()
}
