package com.avito.android.impact

/**
 * pass `<application-module>/build/intermediates/runtime_symbol_list/<variant>/R.txt` lines stream here
 *
 * example format: newlines separated
 *
 * int drawable vip 0x7f080482
 * int id address 0x7f0a005c
 * int styleable AppBarLayout_expanded 4
 * int[] styleable ViewBackgroundHelper { 0x010100d4, 0x7f04005d, 0x7f04005e } //this case is not handled well here, but we don't need it
 */
fun readSymbolList(lines: Sequence<String>): Map<String, Int> {
    return lines.map {
        val split = it.split(" ")
        SymbolListEntry(
            format = split.component1(),
            type = split.component2(),
            name = split.component3(),
            id = split.component4()
        )
    }
        .filter { it.type == "id" }
        .map { it.name to it.id.removePrefix("0x").toInt(16) }
        .toMap()
}

private data class SymbolListEntry(val format: String, val type: String, val name: String, val id: String)

