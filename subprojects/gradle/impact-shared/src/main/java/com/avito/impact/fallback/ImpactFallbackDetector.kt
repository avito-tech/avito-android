package com.avito.impact.fallback

public interface ImpactFallbackDetector {

    public val isFallback: Result

    public sealed class Result {
        public object Run : Result()
        public class Skip(public val reason: String) : Result()
    }
}
