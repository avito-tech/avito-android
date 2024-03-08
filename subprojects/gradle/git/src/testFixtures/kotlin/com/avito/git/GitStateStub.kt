package com.avito.git

public fun GitStateStub(
    currentBranch: Branch,
    originalBranch: Branch = currentBranch,
    targetBranch: Branch? = null
): GitState {
    return GitState(
        originalBranch,
        currentBranch,
        targetBranch,
        "develop",
        false,
    )
}

public fun GitLocalStateStub(
    currentBranch: Branch,
    originalBranch: Branch = currentBranch,
    targetBranch: Branch? = null
): GitState {
    return GitState(
        originalBranch,
        currentBranch,
        targetBranch,
        "develop",
        true,
    )
}
