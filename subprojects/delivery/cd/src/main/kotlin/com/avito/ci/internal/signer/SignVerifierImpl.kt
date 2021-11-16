package com.avito.ci.internal.signer

import com.avito.android.AndroidSdk
import com.avito.utils.ExistingFile

internal class SignVerifierImpl(private val androidSdk: AndroidSdk) : SignVerifier {

    override fun verifyApk(apk: ExistingFile, expectedSha1: String): SignVerifier.Result {
        return androidSdk.keytool.getJarSha1(apk)
            .fold(
                { actualSha1 ->
                    if (actualSha1 == expectedSha1) {
                        SignVerifier.Result.Ok
                    } else {
                        SignVerifier.Result.WrongSignature(expectedSha1, actualSha1)
                    }
                },
                { error -> SignVerifier.Result.VerificationError(error) }
            )
    }

    override fun verifyBundle(bundle: ExistingFile, expectedSha1: String): SignVerifier.Result {
        return androidSdk.keytool.getJarSha1(bundle).fold(
            { actualSha1 ->
                if (actualSha1 == expectedSha1) {
                    SignVerifier.Result.Ok
                } else {
                    SignVerifier.Result.WrongSignature(expectedSha1, actualSha1)
                }
            },
            { error -> SignVerifier.Result.VerificationError(error) }
        )
    }
}
