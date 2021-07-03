package com.avito.android

import java.io.File

public interface Aapt {

    public fun getPackageName(apk: File): Result<String>
}
