package com.avito.android.check

import com.avito.android.AnnotationData
import com.avito.android.toJavaType

/**
 * A typo in @CommandUuid doesn't break a test run, but such a team is not found in TMS
 * and the test case silently stays without an owner.
 */
public class CommandUuidFormatCheck(override val onViolation: (String) -> Unit) : TestSignatureCheck {

    private val commandUuidAnnotationValue = "value"

    override fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    ) {
        (classAnnotations + methodAnnotations)
            .filter { it.name == COMMAND_UUID_ANNOTATION_TYPE }
            .forEach { annotation ->
                val value = annotation.getStringValue(commandUuidAnnotationValue)
                val testLocation = "${className.toJavaType()}.$methodName"

                if (value == null) {
                    onViolation("Can't read @CommandUuid value in $testLocation")
                } else if (!value.trim().matches(uuidRegex)) {
                    onViolation(
                        "Invalid @CommandUuid(\"$value\") in $testLocation: " +
                            "expected team uuid from TMS, for example b5a5ff6d-73ef-400e-abda-6a89b19e4729. " +
                            "Value length is ${value.length}, check for invisible characters if it looks valid"
                    )
                }
            }
    }
}

private val uuidRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

// visible in test
internal const val COMMAND_UUID_ANNOTATION_TYPE = "com.avito.android.test.annotations.CommandUuid"
