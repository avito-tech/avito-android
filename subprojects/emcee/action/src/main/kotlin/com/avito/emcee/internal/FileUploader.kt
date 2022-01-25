package com.avito.emcee.internal

import okhttp3.HttpUrl
import java.io.File

internal interface FileUploader {
    suspend fun upload(file: File): HttpUrl
}
