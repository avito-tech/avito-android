package com.avito.cd

import com.avito.utils.logging.CILogger

internal class CdBuildConfigValidator(
    private val config: CdBuildConfig,
    private val logger: CILogger
) {

    fun validate() {
        warnAboutUnsupportedDeployments()
        checkUniqueGooglePlayDeployments()
    }

    private fun warnAboutUnsupportedDeployments() {
        config.deployments.filterIsInstance<CdBuildConfig.Deployment.Unknown>().forEach {
            logger.info("Ignore unknown CD config deployment: ${it.type}")
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
}
