package com.avito.git

public interface GitState {

    /**
     * Позволяет получить данные до премерджа, если был таковой
     * если нет, должен равняться currentBranch
     */
    public val originalBranch: Branch

    public val currentBranch: Branch

    /**
     * In case of pull request build
     */
    public val targetBranch: Branch?

    public val defaultBranch: String
}

public val GitState.isOnDefaultBranch: Boolean
    get() = currentBranch.name.asBranchWithoutOrigin() == this.defaultBranch.asBranchWithoutOrigin()
