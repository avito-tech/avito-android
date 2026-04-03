package com.avito.android.string_transform.internal.task.aab

import com.android.aapt.Resources
import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File

internal class AabProtobufXmlTransformer(
    private val textFormatTransformer: ProtobufTextFormatTransformer = ProtobufTextFormatTransformer(),
) {

    fun transform(
        inputFile: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> {
        return textFormatTransformer.transform(
            inputFile = inputFile,
            rules = rules,
            parse = { input -> Resources.XmlNode.parseFrom(input) },
        )
    }
}
