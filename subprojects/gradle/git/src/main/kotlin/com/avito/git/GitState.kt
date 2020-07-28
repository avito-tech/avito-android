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
     * In case of pull request build
     */
    val targetBranch: Branch?

    val defaultBranch: String
}

val GitState.isOnDefaultBranch: Boolean
    get() = currentBranch.name.asBranchWithoutOrigin() == this.defaultBranch.asBranchWithoutOrigin()
