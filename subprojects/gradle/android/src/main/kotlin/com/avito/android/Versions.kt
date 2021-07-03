package com.avito.android

public fun versionNamePostfix(versionName: String, versionCode: Int): String =
    if (versionName.trim().isEmpty()) "" else "-$versionName-($versionCode)"
