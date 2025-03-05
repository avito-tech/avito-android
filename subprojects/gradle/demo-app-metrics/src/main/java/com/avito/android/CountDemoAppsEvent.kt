package com.avito.android

import com.avito.android.clickstream.ClickStreamSrcId
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.clickstream.event.ParametrizedClickStreamEvent

public class CountDemoAppsEvent(
    public val count: Int,
) : ClickStreamEvent by ParametrizedClickStreamEvent(
    eventId = 14204,
    version = 0,
    params = mapOf(
        "demo_app_count" to count,
    ),
    srcId = ClickStreamSrcId.ANDROID_DEMO_APPS,
)
