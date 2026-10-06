package com.avito.instrumentation.internal

/**
 * Args dumps are published as CI artifacts, so certificates and private keys must not get there as is
 */
internal fun maskPemValues(args: Map<String, String>): Map<String, String> {
    return args.mapValues { (_, value) ->
        if (value.contains(PEM_HEADER_PREFIX)) MASKED_VALUE else value
    }
}

private const val PEM_HEADER_PREFIX = "-----BEGIN "
private const val MASKED_VALUE = "***"
