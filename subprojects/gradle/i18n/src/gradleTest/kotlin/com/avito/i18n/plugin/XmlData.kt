package com.avito.i18n.plugin

import org.intellij.lang.annotations.Language

@Language("xml")
val ORIGINAL_FILE_CONTENT = """
    <?xml version="1.0" encoding="UTF-8"?><resources>
      <string name="some_string">some string</string>
      <string name="params_string">params %s string</string>
      <string name="params_string">params %s string %d</string>
    </resources>
""".trimIndent()

@Language("xml")
val TRANSLATED_FILE_CONTENT = """
    <?xml version="1.0" encoding="UTF-8"?><resources>
      <string name="some_string">emos gnirts</string>
      <string name="params_string">smarap %s gnirts</string>
      <string name="params_string">smarap %s gnirts %d</string>
    </resources>
""".trimIndent()
