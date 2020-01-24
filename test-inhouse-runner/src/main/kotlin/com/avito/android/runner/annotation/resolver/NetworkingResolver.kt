package com.avito.android.runner.annotation.resolver

import com.avito.android.api.AbstractMockApiRule
import com.avito.android.mock.MockWebServerApiRule

class NetworkingResolver : ClassReflectionResolver(key = NETWORKING_TYPE_KEY, resolver = { aClass ->

    val hasMockApiRule = aClass.declaredFields
        .find { it.type == MockWebServerApiRule::class.java } != null

    val hasMockitoApiRule = aClass.declaredFields
        .find { AbstractMockApiRule::class.java.isAssignableFrom(it.type) } != null

    val networkingType = when {
        hasMockApiRule && hasMockitoApiRule -> NetworkingType.ILLEGAL
        hasMockApiRule -> NetworkingType.MOCK_WEB_SERVER
        hasMockitoApiRule -> NetworkingType.MOCKED_NETWORK_LAYER
        else -> NetworkingType.REAL
    }

    TestMetadataResolver.Resolution.ReplaceSerializable(networkingType)
})

enum class NetworkingType {
    REAL, MOCK_WEB_SERVER, MOCKED_NETWORK_LAYER, ILLEGAL
}

const val NETWORKING_TYPE_KEY = "networking"
