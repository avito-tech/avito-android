package com.avito.android.clickstream

import com.avito.android.Result
import com.avito.android.clickstream.api.ClickStreamEventRequest
import com.avito.android.clickstream.api.NetworkClickStreamEvent
import com.avito.android.clickstream.api.NetworkClickStreamMeta
import com.avito.android.clickstream.event.ClickStreamEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

public interface EventsTracker {

    public fun trackEvent(event: ClickStreamEvent): Result<Unit>
}

public class ClickStreamEventTracker(
    private val clickStreamSender: ClickStreamSender,
    private val clickStreamEventSaturator: ClickStreamEventSaturator? = null,
) : EventsTracker {

    private val fieldConverter = ClickStreamFieldConverter()

    public override fun trackEvent(event: ClickStreamEvent): Result<Unit> {
        return clickStreamSender.sendEvents(envelope = event.toEnvelope())
    }

    private fun ClickStreamEvent.toEnvelope(): ClickStreamEventRequest {
        val commonEnv = mapOf(
            "cdtm" to TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        )

        return ClickStreamEventRequest(
            events = listOf(
                NetworkClickStreamEvent(
                    eventId = eventId,
                    version = version,
                    env = commonEnv + clickStreamEventSaturator?.environment().orEmpty(),
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
