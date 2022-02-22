package com.avito.android.build_verdict

import com.avito.android.build_verdict.internal.Error
import com.avito.android.build_verdict.internal.Error.Multi
import com.avito.android.build_verdict.internal.Error.Single
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertWithMessage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.lang.reflect.Type
import com.avito.android.build_verdict.internal.writer.HtmlBuildVerdictWriter.Companion.fileName as htmlVerdictFileName
import com.avito.android.build_verdict.internal.writer.PlainTextBuildVerdictWriter.Companion.fileName as plainTextVerdictFileName
import com.avito.android.build_verdict.internal.writer.RawBuildVerdictWriter.Companion.fileName as rawVerdictFileName

internal abstract class BaseBuildVerdictTest {

    @field:TempDir
    lateinit var temp: File

    protected val jsonBuildVerdict by lazy {
        File(temp, "outputs/build-verdict/$rawVerdictFileName")
    }
    protected val plainTextBuildVerdict by lazy {
        File(temp, "outputs/build-verdict/$plainTextVerdictFileName")
    }
    protected val htmlBuildVerdict by lazy {
        File(temp, "outputs/build-verdict/$htmlVerdictFileName")
    }

    protected val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            Error::class.java,
            object : JsonDeserializer<Error> {
                override fun deserialize(
                    json: JsonElement,
                    type: Type,
                    context: JsonDeserializationContext
                ): Error {
                    return when {
                        json.asJsonObject.has("errors") -> context.deserialize<Multi>(json, Multi::class.java)
                        else -> context.deserialize<Single>(json, Single::class.java)
                    }
                }
            }
        ).create()

    protected fun generateProject(
        module: Module = AndroidAppModule(
            name = appName
        ),
        buildGradleExtra: String = "",
        imports: List<String> = emptyList(),
        useKts: Boolean = false,
    ) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-verdict")
            },
            modules = listOf(module),
            imports = imports,
            buildGradleExtra = buildGradleExtra,
            useKts = useKts
        ).generateIn(temp)
    }

    protected fun assertBuildVerdictFileExist(
        exist: Boolean
    ) {
        assertWithMessage("$jsonBuildVerdict is ${if (exist) "" else "not"} present")
            .that(jsonBuildVerdict.exists())
            .isEqualTo(exist)

        assertWithMessage("$plainTextBuildVerdict is ${if (exist) "" else "not"} present")
            .that(plainTextBuildVerdict.exists())
            .isEqualTo(exist)

        assertWithMessage("$htmlBuildVerdict is ${if (exist) "" else "not"} present")
            .that(htmlBuildVerdict.exists())
            .isEqualTo(exist)
    }

    protected companion object {
        const val appName = "app"
    }
}
