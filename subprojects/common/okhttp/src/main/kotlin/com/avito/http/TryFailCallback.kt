package com.avito.http

import okhttp3.Request

public interface TryFailCallback {

    public fun onTryFail(
        attemptNumber: Int,
        request: Request,
        exception: Throwable
    )

    public companion object {

        public val STUB: TryFailCallback = object : TryFailCallback {

            override fun onTryFail(
                attemptNumber: Int,
                request: Request,
                exception: Throwable
            ) {
            }
        }

        public fun TryFailCallback.combine(callback: TryFailCallback): TryFailCallback =
            object : TryFailCallback {
                override fun onTryFail(attemptNumber: Int, request: Request, exception: Throwable) {
                    this@combine.onTryFail(attemptNumber, request, exception)
                    callback.onTryFail(attemptNumber, request, exception)
                }
            }
    }
}
