package com.avito.git

public class Branch(
    public val name: String,
    public val commit: String,
    remoteName: String = defaultRemote
) {

    public val fullName: String = if (remoteName.isNotBlank()) {
        "$remoteName/$name"
    } else {
        name
    }

    override fun toString(): String = "branch: $name ($commit)"
}

internal const val defaultRemote = "origin"

internal fun String.asBranchWithoutOrigin() = substringAfter("$defaultRemote/")

internal fun String.asOriginBranch(): String {
    return if (startsWith("$defaultRemote/")) {
        this
    } else {
        "$defaultRemote/$this"
    }
}
