package com.avito.ci.internal.signer

import com.avito.utils.ExistingFile

internal interface SignVerifier {

    sealed class Result {

        object Ok : Result()

        data class WrongSignature(val expectedSha1: String, val actualSha1: String) : Result()

        data class VerificationError(val exception: Throwable) : Result()
    }

    fun verifyApk(apk: ExistingFile, expectedSha1: String): Result

    fun verifyBundle(bundle: ExistingFile, expectedSha1: String): Result
}
