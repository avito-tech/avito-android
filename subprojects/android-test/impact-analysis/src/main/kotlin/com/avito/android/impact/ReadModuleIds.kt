package com.avito.android.impact

/**
 * pass all `<library-module>/build/intermediates/symbol_list_with_package_name/<variant>/package-aware-r.txt` files here
 *
 * example format:
 *
 * com.avito.android.remote.notification
 * drawable ic_notification
 * id message
 * id refresh
 * layout notification_center_load_snippet
 * string notification_direct_reply
 */
fun readModuleIds(rFilesLines: Map<String, Sequence<String>>): Map<String, List<String>> {
    return rFilesLines.map { (moduleName, lines) ->
        moduleName to lines
            .drop(1) // don't need package name
            .filter { it.startsWith("id ") }
            .map { line -> line.split(" ")[1] }
            .toList()
    }.toMap()
}
