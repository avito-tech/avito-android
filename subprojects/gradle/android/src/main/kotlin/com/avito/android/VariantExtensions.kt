package com.avito.android

import com.android.build.api.variant.Variant
import com.avito.capitalize

public fun Variant.capitalizedName(): String = name.capitalize()
