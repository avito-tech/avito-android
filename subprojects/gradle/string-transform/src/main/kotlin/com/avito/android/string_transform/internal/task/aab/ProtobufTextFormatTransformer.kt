package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Message
import java.io.File
import java.io.InputStream

internal class ProtobufTextFormatTransformer {

    fun <T : Message> transform(
        inputFile: File,
        rules: List<NormalizedRule>,
        parse: (InputStream) -> T,
    ): Result<Unit> = Result.tryCatch {
        val originalMessage = inputFile.inputStream().use(parse)
        val builder = originalMessage.toBuilder()

        if (transformMessage(builder, rules)) {
            inputFile.outputStream().use { output ->
                builder.build().writeTo(output)
            }
        }
    }

    private fun transformMessage(
        builder: Message.Builder,
        rules: List<NormalizedRule>,
    ): Boolean {
        val fields = builder.allFields.keys.toList()
        return fields.fold(false) { changed, field ->
            transformField(builder, field, rules) || changed
        }
    }

    private fun transformField(
        builder: Message.Builder,
        field: FieldDescriptor,
        rules: List<NormalizedRule>,
    ): Boolean {
        return when {
            field.isRepeated -> transformRepeatedField(builder, field, rules)
            field.javaType == FieldDescriptor.JavaType.STRING -> {
                val originalValue = builder.getField(field) as String
                val transformedValue = applyRules(originalValue, rules)
                if (transformedValue != originalValue) {
                    builder.setField(field, transformedValue)
                    true
                } else {
                    false
                }
            }
            field.javaType == FieldDescriptor.JavaType.MESSAGE -> {
                val nestedMessage = builder.getField(field) as Message
                val nestedBuilder = nestedMessage.toBuilder()
                if (transformMessage(nestedBuilder, rules)) {
                    builder.setField(field, nestedBuilder.build())
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun transformRepeatedField(
        builder: Message.Builder,
        field: FieldDescriptor,
        rules: List<NormalizedRule>,
    ): Boolean {
        val originalValues = builder.getField(field) as List<*>
        return when (field.javaType) {
            FieldDescriptor.JavaType.STRING -> {
                val transformedValues = originalValues.map { applyRules(it as String, rules) }
                if (transformedValues != originalValues) {
                    builder.clearField(field)
                    transformedValues.forEach { value ->
                        builder.addRepeatedField(field, value)
                    }
                    true
                } else {
                    false
                }
            }
            FieldDescriptor.JavaType.MESSAGE -> {
                val transformedValues = originalValues.map { value ->
                    val nestedBuilder = (value as Message).toBuilder()
                    transformMessage(nestedBuilder, rules)
                    nestedBuilder.build()
                }
                if (transformedValues != originalValues) {
                    builder.clearField(field)
                    transformedValues.forEach { value ->
                        builder.addRepeatedField(field, value)
                    }
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun applyRules(value: String, rules: List<NormalizedRule>): String {
        return rules.fold(value) { current, rule ->
            current.replace(rule.from, rule.to)
        }
    }
}
