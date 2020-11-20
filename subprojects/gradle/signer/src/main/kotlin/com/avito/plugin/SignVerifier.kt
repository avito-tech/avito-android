package com.avito.plugin

import com.avito.android.AndroidSdk
import com.avito.utils.ExistingFile

interface SignVerifier {

    sealed class Result {
        object Ok : Result()
        data class WrongSignature(val expectedSha1: String, val actualSha1: String) : Result()
        data class VerificationError(val exception: Throwable) : Result()
    }

    fun verifyApk(apk: ExistingFile, expectedSha1: String): Result

    fun verifyBundle(bundle: ExistingFile, expectedSha1: String): Result

    class Impl(private val androidSdk: AndroidSdk) : SignVerifier {

        override fun verifyApk(apk: ExistingFile, expectedSha1: String): Result {
            return androidSdk.keytool.getJarSha1(apk)
                .fold(
                    { actualSha1 ->
                        if (actualSha1 == expectedSha1) {
                            Result.Ok
                        } else {
                            Result.WrongSignature(expectedSha1, actualSha1)
                        }
                    },
                    { error -> Result.VerificationError(error) }
                )
        }

        override fun verifyBundle(bundle: ExistingFile, expectedSha1: String): Result {
            return androidSdk.keytool.getJarSha1(bundle).fold(
                { actualSha1 ->
                    if (actualSha1 == expectedSha1) {
                        Result.Ok
                    } else {
                        Result.WrongSignature(expectedSha1, actualSha1)
                    }
                },
                { error -> Result.VerificationError(error) }
            )
        }
    }
}
