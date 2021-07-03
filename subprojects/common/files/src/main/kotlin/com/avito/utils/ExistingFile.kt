package com.avito.utils

import java.io.File

public fun File.toExisting(): ExistingFile =
    ExistingFileImpl(this)

/**
 * A way to represent in type system that client has no need to check file existence
 */
public interface ExistingFile {

    public val file: File
}
