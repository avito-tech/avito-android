package com.avito.android.clickstream.event

import com.avito.android.clickstream.ClickStreamSrcId

public interface ClickStreamEvent {
    public val eventId: Int
    public val version: Int
    public val params: Map<String, Any>
    public val srcId: ClickStreamSrcId
}

public data class ParametrizedClickStreamEvent(
    override val eventId: Int,
    override val version: Int,
    override val params: Map<String, Any>,
    override val srcId: ClickStreamSrcId,
) : ClickStreamEvent {

    override fun toString(): String {
        return "ParametrizedClickStreamEvent (id:$eventId, params:$params, version:$version)"
    }
}
