package com.avito.ci

import com.avito.android.AndroidSdk
import com.avito.ci.internal.signer.SignVerifier
import com.avito.utils.ExistingFile
import com.avito.utils.ExistingFileImpl
import java.io.File

internal class OutputsVerifier(
    private val androidSdk: AndroidSdk,
    private val signVerifier: SignVerifier,
    private val outputsDir: File
) {

    private val File.relativeToOutputsPath: String
        get() = toRelativeString(outputsDir)

    private val ExistingFile.relativeToOutputsPath: String
        get() = file.toRelativeString(outputsDir)

    val errors = mutableListOf<String>()

    fun requireFile(file: File, shouldNotBeEmpty: Boolean = true, action: (ExistingFile) -> Unit = {}) {
        if (!file.exists()) {
            errors += "Artifact: ${file.relativeToOutputsPath} not found"
        } else if (shouldNotBeEmpty && file.length() == 0L) {
            errors += "Artifact: ${file.relativeToOutputsPath} size == 0"
        } else {
            action.invoke(ExistingFileImpl(file))
        }
    }

    fun checkApkSignature(apkFile: ExistingFile, expectedSha1: String) {
        when (val result = signVerifier.verifyApk(apkFile, expectedSha1)) {
            is SignVerifier.Result.Ok -> {
                // do nothing
            }
            is SignVerifier.Result.WrongSignature -> errors +=
                "${apkFile.relativeToOutputsPath} signature doesn't match:\n" +
                    "${result.actualSha1} <- actual\n" +
                    "${result.expectedSha1} <- should be"
            is SignVerifier.Result.VerificationError ->
                errors += "cannot check ${apkFile.relativeToOutputsPath} signature, ${result.exception.message}"
        }
    }

    fun checkBundleSignature(bundleFile: ExistingFile, expectedSha1: String) {
        when (val result = signVerifier.verifyBundle(bundleFile, expectedSha1)) {
            SignVerifier.Result.Ok -> {
                // do nothing
            }
            is SignVerifier.Result.WrongSignature -> errors +=
                "${bundleFile.relativeToOutputsPath} signature doesn't match:\n" +
                    "${result.actualSha1} <- actual\n" +
                    "${result.expectedSha1} <- should be"
            is SignVerifier.Result.VerificationError ->
                errors += "cannot check ${bundleFile.relativeToOutputsPath} signature, ${result.exception.message}"
        }
    }

    fun checkPackageName(apkFile: File, expectedPackage: String) {
        androidSdk.aapt.getPackageName(apkFile).fold(
            { actualPackage ->
                if (actualPackage != expectedPackage) {
                    errors +=
                        "${apkFile.relativeToOutputsPath} package doesn't match:\n" +
                            "$actualPackage <- actual\n" +
                            "$expectedPackage <- should be"
                }
            },
            { errors += "cannot check ${apkFile.relativeToOutputsPath} signature, ${it.message}" }
        )
    }
}
