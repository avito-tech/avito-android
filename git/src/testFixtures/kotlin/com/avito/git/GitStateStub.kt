package com.avito.git

class GitStateStub(
    override val currentBranch: Branch,
    override val originalBranch: Branch = currentBranch,
    override val targetBranch: Branch? = null
) : GitState {
    override val defaultBranch = "develop"
}

class GitLocalStateStub(
    override val currentBranch: Branch,
    override val originalBranch: Branch = currentBranch,
    override val targetBranch: Branch? = null
) : GitLocalState {
    override val defaultBranch = "develop"
}
