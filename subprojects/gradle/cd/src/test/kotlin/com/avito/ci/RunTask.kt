package com.avito.ci

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import java.io.File

internal fun runTask(
    projectDir: File,
    vararg args: String,
    branch: String = "develop",
    dryRun: Boolean = true,
    expectedFailure: Boolean = false
): TestResult {
    return ciRun(
        projectDir,
        *args,
        "-PdeviceName=LOCAL",
        "-PteamcityBuildId=100",
        "-PappA.versionName=1",
        "-PappA.versionCode=1",
        "-PappB.versionName=1",
        "-PappB.versionCode=1",
        "-Pavito.bitbucket.url=http://bitbucket",
        "-Pavito.bitbucket.projectKey=AA",
        "-Pavito.bitbucket.repositorySlug=android",
        "-Pavito.stats.enabled=false",
        "-Pavito.stats.host=http://stats",
        "-Pavito.stats.fallbackHost=http://stats",
        "-Pavito.stats.port=80",
        "-Pavito.stats.namespace=android",
        "-PkubernetesToken=stub",
        "-PkubernetesUrl=stub",
        "-PkubernetesCaCertData=stub",
        branch = branch,
        dryRun = dryRun,
        expectFailure = expectedFailure
    )
}
