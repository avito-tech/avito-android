package com.avito.android.tech_budget.deeplinks

import java.io.File

public interface DeepLinksFileParser {

    public fun parse(file: File): List<DeepLink>
}
