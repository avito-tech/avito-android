package com.avito.android.network_contracts.output

public fun interface OutputTransformer {

    public fun transform(text: String): String
}
