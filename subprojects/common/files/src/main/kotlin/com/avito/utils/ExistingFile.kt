package com.avito.utils

import java.io.File

// Способ перенести ответственность за проверки существования файла на клиента и отразить это в API

fun File.toExisting(): ExistingFile =
    ExistingFile.Impl(this)

interface ExistingFile {

    val file: File

    class Impl(file: File) : ExistingFile {

        override val file = file
            get() {
                require(field.exists()) { "${field.path} must exists" }
                require(field.isFile) { "${field.path} must be a file" }
                return field
            }

        constructor(directory: ExistingDirectory, fileName: String) : this(File(directory.dir, fileName))

        override fun toString(): String = file.toString()
    }

    // todo move to test source
    object Stub : ExistingFile {
        override val file: File
            get() = TODO("not implemented")
    }
}

interface ExistingDirectory {

    val dir: File

    operator fun plus(path: String): ExistingDirectory

    fun file(name: String): ExistingFile

    class Impl(dir: File) : ExistingDirectory {

        override val dir = dir
            get() {
                require(field.exists()) { "${field.path} must exists" }
                require(field.isDirectory) { "${field.path} must be a directory" }
                return field
            }

        constructor(path: String) : this(File(path))

        override operator fun plus(path: String): ExistingDirectory =
            Impl(File(dir, path))

        override fun file(name: String): ExistingFile =
            ExistingFile.Impl(this, name)

        override fun toString(): String = dir.toString()
    }

    // todo move to test source
    object Stub : ExistingDirectory {
        override val dir: File
            get() = TODO("not implemented")

        override fun plus(path: String): ExistingDirectory =
            Stub

        override fun file(name: String): ExistingFile =
            ExistingFile.Stub
    }
}
