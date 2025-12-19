package com.avito.android.model.input.config.validator

import com.avito.android.model.input.config.CdBuildConfigV2

internal class QappsCdBuildConfigValidator : CdBuildConfigValidator<CdBuildConfigV2> {

    override fun validate(config: CdBuildConfigV2) {
        checkQappsDeployments(config)
    }

    private fun checkQappsDeployments(config: CdBuildConfigV2) {
        val qappsDeployments = config.deployments.filterIsInstance<CdBuildConfigV2.Deployment.Qapps>()
        require(qappsDeployments.size <= 1) {
            "Must be one Qapps deployment, but was: $qappsDeployments"
        }
        if (qappsDeployments.isNotEmpty()) {
            require(config.schemaVersion == 2L) {
                "Qapps deployments is supported only in the 2'nd version of contract"
            }
        }
    }
}
