package com.avito.utils

import java.io.File

public class ExistingDirectoryImpl(dir: File) : ExistingDirectory {

    override val dir: File = dir
        get() {
            require(field.exists()) { "${field.path} must exists" }
            require(field.isDirectory) { "${field.path} must be a directory" }
            return field
        }

    override operator fun plus(path: String): ExistingDirectory =
        ExistingDirectoryImpl(File(dir, path))

    override fun file(name: String): ExistingFile =
        ExistingFileImpl(this, name)

    override fun toString(): String = dir.toString()
}
