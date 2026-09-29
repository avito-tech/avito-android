package com.avito.android.feedback

import com.avito.android.Result

/**
 * `sendEvent` does not authenticate the caller and expects a login in the body, so it is resolved here.
 *
 * Failure carries the reason: "login is unknown" alone is not actionable.
 */
public fun interface UsernameResolver {

    public fun resolve(): Result<String>
}
