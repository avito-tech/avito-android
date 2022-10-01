package com.avito.android.model.input

import java.io.File

internal sealed interface Deployment {
    public val file: File
}
