package com.avito.android.string_transform.internal.configuration

import com.avito.android.Problem
import com.avito.android.asRuntimeException

internal class SigningCapabilityValidator(
    private val projectPath: String,
    private val pipelineName: String,
    private val variantName: String,
) {

    fun validate(
        applicationId: String,
        apkToken: String?,
        bundleToken: String?,
    ): ValidatedSigningTokens {
        if (apkToken.isNullOrBlank()) {
            throw Problem.Builder(
                shortDescription = "String-transform pipeline '$pipelineName' cannot enable signing for APK",
                context = "Configuring signing adapter for $projectPath variant '$variantName'"
            )
                .because("No APK signing token is configured in signer for application id '$applicationId'.")
                .addSolution("Add signer.apkSignTokens.put(\"$applicationId\", \"<token>\") to the module.")
                .build()
                .asRuntimeException()
        }

        if (bundleToken.isNullOrBlank()) {
            throw Problem.Builder(
                shortDescription = "String-transform pipeline '$pipelineName' cannot enable signing for AAB",
                context = "Configuring signing adapter for $projectPath variant '$variantName'"
            )
                .because("No bundle signing token is configured in signer for application id '$applicationId'.")
                .addSolution("Add signer.bundleSignTokens.put(\"$applicationId\", \"<token>\") to the module.")
                .build()
                .asRuntimeException()
        }

        return ValidatedSigningTokens(
            apkToken = apkToken,
            bundleToken = bundleToken,
        )
    }
}

internal data class ValidatedSigningTokens(
    val apkToken: String,
    val bundleToken: String,
)
