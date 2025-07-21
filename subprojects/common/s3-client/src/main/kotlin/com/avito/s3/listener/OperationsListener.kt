package com.avito.s3.listener

import java.time.Duration

public interface OperationsListener {
    public fun onOperationSuccess(
        operationName: String,
        duration: Duration,
    )

    public fun onOperationFailure(
        throwable: Throwable,
        operationName: String,
        duration: Duration,
    )
}
