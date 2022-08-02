package com.avito.android.model.input

import java.io.File

public sealed class Deployment {
    public abstract val file: File
}
