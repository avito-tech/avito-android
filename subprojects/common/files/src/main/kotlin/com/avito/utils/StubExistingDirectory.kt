package com.avito.utils

import java.io.File

// todo move to test source
public object StubExistingDirectory : ExistingDirectory {
    override val dir: File
        get() = TODO("not implemented")

    override fun plus(path: String): ExistingDirectory = StubExistingDirectory

    override fun file(name: String): ExistingFile = StubExistingFile
}
