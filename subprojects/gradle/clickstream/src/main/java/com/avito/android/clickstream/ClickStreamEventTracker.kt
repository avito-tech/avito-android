package com.avito.android.clickstream

import com.avito.android.clickstream.api.ClickStreamEventRequest
import com.avito.android.clickstream.api.NetworkClickStreamEnv
import com.avito.android.clickstream.api.NetworkClickStreamEvent
import com.avito.android.clickstream.api.NetworkClickStreamMeta
import com.avito.android.clickstream.event.ClickStreamEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

public class ClickStreamEventTracker(
    private val clickStreamEventService: ClickStreamEventService,
) {

    private val fieldConverter = ClickStreamFieldConverter()

    public fun trackEvent(event: ClickStreamEvent) {
        clickStreamEventService.sendEvents(envelope = event.toEnvelope())
    }

    private fun ClickStreamEvent.toEnvelope(): ClickStreamEventRequest {
        return ClickStreamEventRequest(
            events = listOf(
                NetworkClickStreamEvent(
                    eventId = eventId,
                    version = version,
                    env = NetworkClickStreamEnv(
                        cdtm = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                    ),
                    params = fieldConverter.convertFields(params),
                )
            ),
            meta = NetworkClickStreamMeta(
                sdk = "android/0.0.0",
                buildUid = UUID.randomUUID().toString(),
                srcId = srcId.value,
            )
        )
    }
}
