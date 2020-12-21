package com.avito.utils.logging

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class StubCILogger(
    val debugHandler: RecordingLoggingHandler = RecordingLoggingHandler(),
    val infoHandler: RecordingLoggingHandler = RecordingLoggingHandler(),
    val warnHandler: RecordingLoggingHandler = RecordingLoggingHandler(),
    val criticalHandler: RecordingLoggingHandler = RecordingLoggingHandler()
) : CILogger(
    debugHandler = debugHandler,
    infoHandler = infoHandler,
    warnHandler = warnHandler,
    criticalHandler = criticalHandler
)
