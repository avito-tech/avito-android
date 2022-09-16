package com.avito.android.model.input

import java.io.File

public sealed interface Deployment {
    public val file: File
}
