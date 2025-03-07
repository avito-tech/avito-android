package com.avito.i18n.plugin

import org.intellij.lang.annotations.Language

@Language("xml")
val ORIGINAL_FILE_CONTENT = """
    <?xml version="1.0" encoding="UTF-8"?>
    <resources>
      <string name="some_string">some string</string>
      <string name="params_string">params %s string %d</string>
    </resources>
""".trimIndent()

@Language("xml")
val TRANSLATED_FILE_CONTENT = """
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <resources>
        <string hash="8b45e4bd1c6acb88bebf6407d16205f567e62a3e" name="some_string">emos gnirts</string>
        <string hash="3c72027a032b4dd679f37190087323a78c0c2817" name="params_string">smarap %s gnirts %d</string>
    </resources>
""".trimIndent()
