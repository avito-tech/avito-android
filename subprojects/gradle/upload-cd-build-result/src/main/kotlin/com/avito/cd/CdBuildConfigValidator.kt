package com.avito.cd

internal class CdBuildConfigValidator(private val config: CdBuildConfig) {

    fun validate() {
        checkUnsupportedDeployments()
        checkUniqueGooglePlayDeployments()
        checkQappsDeployments()
    }

    private fun checkUnsupportedDeployments() {
        val unknownDeployments = config.deployments.filterIsInstance<CdBuildConfig.Deployment.Unknown>()
        require(unknownDeployments.isEmpty()) {
            "Unknown deployment types: $unknownDeployments"
        }
    }

    private fun checkUniqueGooglePlayDeployments() {
        val googlePlayDeployments = config.deployments.filterIsInstance<CdBuildConfig.Deployment.GooglePlay>()
        val deploysByVariant = googlePlayDeployments.groupBy(CdBuildConfig.Deployment.GooglePlay::buildVariant)
        deploysByVariant.forEach { (_, deploys) ->
            require(deploys.size == 1) {
                "Must be one deploy per variant, but was: $googlePlayDeployments"
            }
        }
    }

    private fun checkQappsDeployments() {
        val deployments = config.deployments.filterIsInstance<CdBuildConfig.Deployment.Qapps>()
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
