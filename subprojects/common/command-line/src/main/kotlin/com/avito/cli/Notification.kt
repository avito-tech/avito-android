package com.avito.cli

public sealed interface Notification {
    public data class Output(public val line: String) : Notification
    public data class Exit(public val output: String) : Notification
}
