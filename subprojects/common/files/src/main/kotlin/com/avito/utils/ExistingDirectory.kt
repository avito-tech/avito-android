package com.avito.utils

import java.io.File

public interface ExistingDirectory {

    public val dir: File

    public operator fun plus(path: String): ExistingDirectory

    public fun file(name: String): ExistingFile
}
