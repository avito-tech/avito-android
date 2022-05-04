package com.avito.emcee.client.internal

import okhttp3.HttpUrl
import java.io.File

internal interface FileUploader {

    suspend fun upload(file: File): HttpUrl
}
