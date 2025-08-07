package com.avito.android.clickstream

import com.avito.android.Result
import com.avito.android.clickstream.api.ClickStreamEventRequest

public interface ClickStreamSender {

    public fun sendEvents(envelope: ClickStreamEventRequest): Result<Unit>
}
