package com.avito.android.model.input.v2

import com.avito.android.model.input.CdBuildConfigV2

internal interface CdBuildConfigValidator {

    fun validate(config: CdBuildConfigV2)
}

internal class StrictCdBuildConfigValidator : CdBuildConfigValidator {

    override fun validate(config: CdBuildConfigV2) {
        checkQappsDeployments(config)
    }

    private fun checkQappsDeployments(config: CdBuildConfigV2) {
        val deployments = config.deployments.filterIsInstance<CdBuildConfigV2.Deployment.Qapps>()
        require(deployments.size <= 1) {
            "Must be one Qapps deployment, but was: $deployments"
        }
        if (deployments.isNotEmpty()) {
            require(config.schemaVersion >= 2) {
                "Qapps deployments is supported only in the 2'nd version of contract"
            }
        }
    }
}
