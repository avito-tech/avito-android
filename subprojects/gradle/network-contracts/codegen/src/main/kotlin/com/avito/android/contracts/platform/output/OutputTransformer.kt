package com.avito.android.contracts.platform.output

public fun interface OutputTransformer {

    public fun transform(text: String): String
}
