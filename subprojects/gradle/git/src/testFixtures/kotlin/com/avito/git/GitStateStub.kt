package com.avito.git

public class GitStateStub(
    override val currentBranch: Branch,
    override val originalBranch: Branch = currentBranch,
    override val targetBranch: Branch? = null
) : GitState {
    override val defaultBranch: String = "develop"
}

public class GitLocalStateStub(
    override val currentBranch: Branch,
    override val originalBranch: Branch = currentBranch,
    override val targetBranch: Branch? = null
) : GitLocalState {
    override val defaultBranch: String = "develop"
}
