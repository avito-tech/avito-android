package com.avito.android.plugin.internal

import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportList

internal fun KtImportList.findImport(clazz: String): KtImportDirective? {
    return imports.find { import ->
        import.importedReference?.text == clazz
    }
}
