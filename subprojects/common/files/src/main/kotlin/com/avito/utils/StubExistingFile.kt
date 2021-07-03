package com.avito.utils

import java.io.File

// todo move to test source
public object StubExistingFile : ExistingFile {
    override val file: File
        get() = TODO("not implemented")
}
