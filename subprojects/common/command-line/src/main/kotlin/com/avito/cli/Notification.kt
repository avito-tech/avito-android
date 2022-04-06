package com.avito.cli

public sealed class Notification {
    public class Output(public val line: String) : Notification()
    public class Exit(public val output: String) : Notification()
}
