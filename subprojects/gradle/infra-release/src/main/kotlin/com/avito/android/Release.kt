package com.avito.android

import com.avito.git.Git
import com.avito.logger.Logger

class Release(
    private val git: Git,
    private val logger: Logger
) {

    fun release(
        releaseTag: String,
        previousReleaseTag: String,
        currentBranch: String
    ) {
        logger.info("Creating release draft $releaseTag")

        if (currentBranch != releaseTag) {
            logger.warn("Release initiated from $currentBranch, but should run on release branch: $releaseTag")
            return
        }

        //tag
        //push

        val log = git.log(beginTag = previousReleaseTag, endTag = releaseTag)

        log.fold(
            { logger.info(it) },
            { error -> logger.critical("Can't get commit log", error) }
        )

        //gh release create
    }
}
