package com.avito.utils.gradle

public sealed class Environment {
    public object Local : Environment()
    public object Mirakle : Environment()
    public object CI : Environment()
    public object Unknown : Environment()
}
