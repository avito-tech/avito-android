package com.avito.impact.fallback

interface ImpactFallbackDetector {
    val isFallback: Result

    sealed class Result {
        object Run : Result()
        class Skip(val reason: String) : Result()
    }
}
