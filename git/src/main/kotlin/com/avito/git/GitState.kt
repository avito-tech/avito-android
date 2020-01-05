@file:Suppress("UnstableApiUsage")

package com.avito.git

interface GitState {

    /**
     * Позволяет получить данные до премерджа, если был таковой
     * если нет, должен равняться currentBranch
     */
    val originalBranch: Branch

    val currentBranch: Branch

    /**
     * Если билд запускается в контексте пулл-реквеста, то этот параметр содержит target ветку
     */
    val targetBranch: Branch?

    val defaultBranch: String
}

val GitState.isOnDefaultBranch: Boolean
    get() = currentBranch.name.asBranchWithoutOrigin() == this.defaultBranch.asBranchWithoutOrigin()

val GitState.isOnReleaseBranch: Boolean
    get() {
        val branch = currentBranch.name.asBranchWithoutOrigin()
        return branch.startsWith("release/") || branch.startsWith("domofond-release-")
    }
