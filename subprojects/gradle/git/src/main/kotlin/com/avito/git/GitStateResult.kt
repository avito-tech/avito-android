package com.avito.git

/**
 * Outcome of resolving git state lazily via [GitInfoBuildService.getGitStateResult].
 *
 * Replaces the ambiguous nullable `getGitStateOrNull()`, whose `null` collapsed every failure mode
 * (no repository, `git` exit != 0, detached / unborn HEAD, shallow clone missing the ref, unknown
 * strategy) into a single value. Callers can now inspect [Unavailable.cause] and decide explicitly
 * instead of inventing their own string fallbacks at each call site.
 */
public sealed interface GitStateResult {

    public data class Available(val state: GitState) : GitStateResult

    public data class Unavailable(val cause: Throwable) : GitStateResult
}
