package com.avito.deeplink_generator

import com.avito.capitalize
import com.avito.deeplink_generator.internal.parser.DeeplinkFileParser
import com.avito.deeplink_generator.internal.validator.PublicDeeplinksValidator
import com.avito.deeplink_generator.model.Deeplink
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Validates that deeplinks which are marked as public in code, also presented in build script, and vice versa.
 *
 * Deeplinks from code can be stored in file and supplied with [publicDeeplinksFromCode].
 */
public abstract class ValidatePublicDeeplinksTask : DefaultTask() {

    /**
     * Input file with public deeplinks extracted from code.
     * Supported format is specified in [DeeplinkFileParser].
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:Optional
    public abstract val publicDeeplinksFromCode: RegularFileProperty

    /**
     * Hint for user to fix their code if it does not contain deeplinks from build script.
     *
     * Example: `Please, add isPublic = true to @DeeplinkMeta annotation for all deeplinks listed above.`
     */
    @get:Input
    @get:Optional
    public abstract val codeFixHint: Property<String>

    @get:Input
    public abstract val defaultScheme: Property<String>

    @get:Input
    public abstract val publicDeeplinksFromBuildScript: SetProperty<Deeplink>

    @TaskAction
    public fun validate() {
        val generatedDeeplinks = extractPublicDeeplinksFromCode()
        val deeplinksFromBuildScript = publicDeeplinksFromBuildScript.get()
        val codeFixHint = codeFixHint.orNull ?: ""
        PublicDeeplinksValidator.validate(generatedDeeplinks, deeplinksFromBuildScript, codeFixHint)
    }

    private fun extractPublicDeeplinksFromCode(): Set<Deeplink> {
        val publicDeeplinksFromCodeFile = publicDeeplinksFromCode.orNull?.asFile

        requireNotNull(publicDeeplinksFromCodeFile) {
            """
                You must provide `publicDeeplinksFromCodeFile` property in your gradle module to run this task:
                deeplinkGenerator { 
                    publicDeeplinksFromCodeFile.set(...)
                }
            """.trimIndent()
        }
        return DeeplinkFileParser.parse(publicDeeplinksFromCodeFile, defaultScheme.get())
    }

    public companion object {
        public fun taskName(variantName: String): String = "validate${variantName.capitalize()}PublicDeeplinks"
    }
}
