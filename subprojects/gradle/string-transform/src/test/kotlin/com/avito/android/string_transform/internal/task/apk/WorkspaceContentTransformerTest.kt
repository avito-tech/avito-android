package com.avito.android.string_transform.internal.task.apk

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class WorkspaceContentTransformerTest {

    private val transformer = WorkspaceContentTransformer(ZeroByteTextFileDetector())
    private val broadRules = listOf(
        NormalizedRule(from = "samplevalue", to = "changedvalue"),
        NormalizedRule(from = "markpart", to = "nextpart"),
    )

    @Test
    fun `transform - rewrites asset text file - when content contains matching literals`(
        @TempDir dir: File,
    ) {
        val assetFile = writeTextFile(
            dir = dir,
            relativePath = "assets/payload.txt",
            content = "samplevalue markpart",
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(assetFile.readText()).isEqualTo("changedvalue nextpart")
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `transform - rewrites smali file - when content contains matching literals`(
        @TempDir dir: File,
    ) {
        val smaliFile = writeTextFile(
            dir = dir,
            relativePath = "smali/com/example/SomeClass.smali",
            content = "Lcom/example/samplevalue/markpart;",
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(smaliFile.readText()).isEqualTo("Lcom/example/changedvalue/nextpart;")
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `transform - rewrites manifest file - when content contains matching literals`(
        @TempDir dir: File,
    ) {
        val manifestFile = writeTextFile(
            dir = dir,
            relativePath = "AndroidManifest.xml",
            content = """<manifest package="com.example.markpart" />""",
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(manifestFile.readText()).isEqualTo("""<manifest package="com.example.nextpart" />""")
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `transform - preserves unsupported binary file and reports warning - when matched literal is present`(
        @TempDir dir: File,
    ) {
        val binaryFile = writeBinaryFile(
            dir = dir,
            relativePath = "assets/blob.bin",
            content = "samplevalue".toByteArray() + byteArrayOf(0x00, 0x42),
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(binaryFile.readBytes())
            .isEqualTo("samplevalue".toByteArray() + byteArrayOf(0x00, 0x42))
        assertThat(warnings).containsExactly(
            OperationWarning(
                message = "Skipped unsupported binary file during content transform " +
                    "because binary file could not be safely treated as text. " +
                    "Matched literals: samplevalue",
                affectedPath = "assets/blob.bin",
            ),
        )
    }

    @Test
    fun `transform - skips warning for unsupported binary file - when no matched literals are present`(
        @TempDir dir: File,
    ) {
        val binaryFile = writeBinaryFile(
            dir = dir,
            relativePath = "assets/blob.bin",
            content = "unrelated".toByteArray() + byteArrayOf(0x00, 0x42),
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(binaryFile.readBytes())
            .isEqualTo("unrelated".toByteArray() + byteArrayOf(0x00, 0x42))
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `transform - preserves shared object and reports warning - when matched literal is present`(
        @TempDir dir: File,
    ) {
        val nativeLibrary = writeBinaryFile(
            dir = dir,
            relativePath = "lib/arm64-v8a/libsample.so",
            content = "samplevalue markpart".toByteArray(),
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(nativeLibrary.readBytes()).isEqualTo("samplevalue markpart".toByteArray())
        assertThat(warnings).containsExactly(
            OperationWarning(
                message = "Skipped unsupported binary file during content transform " +
                    "because '.so' files are not supported for content transform yet. " +
                    "Matched literals: samplevalue, markpart",
                affectedPath = "lib/arm64-v8a/libsample.so",
            ),
        )
    }

    @Test
    fun `transform - preserves shared object and does not report warning - when no matched literals are present`(
        @TempDir dir: File,
    ) {
        val nativeLibrary = writeBinaryFile(
            dir = dir,
            relativePath = "lib/arm64-v8a/libsample.so",
            content = "unrelated".toByteArray(),
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(nativeLibrary.readBytes()).isEqualTo("unrelated".toByteArray())
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `transform - preserves protobuf binary and reports warning - when matched literal is present`(
        @TempDir dir: File,
    ) {
        val protobufBinary = writeBinaryFile(
            dir = dir,
            relativePath = "BundleConfig.pb",
            content = "samplevalue".toByteArray(),
        )

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = broadRules,
        ).getOrThrow()

        assertThat(protobufBinary.readBytes()).isEqualTo("samplevalue".toByteArray())
        assertThat(warnings).containsExactly(
            OperationWarning(
                message = "Skipped unsupported binary file during content transform " +
                    "because '.pb' files are treated as unsupported binary content. " +
                    "Matched literals: samplevalue",
                affectedPath = "BundleConfig.pb",
            ),
        )
    }

    @Test
    fun `transform - rewrites values xml - when resource identifiers and values contain matching literals`(
        @TempDir dir: File,
    ) {
        val stringsFile = dir.resolve("res/values/strings.xml").apply {
            parentFile.mkdirs()
            writeText(
                """
                <resources>
                    <string name="samplevalue_title">hello samplevalue</string>
                    <string-array name="samplevalue_labels">
                        <item>samplevalue one</item>
                    </string-array>
                    <item type="string" name="samplevalue_subtitle">samplevalue subtitle</item>
                    <color name="bg_redesign_input_samplevalue_black">#000000</color>
                </resources>
                """.trimIndent()
            )
        }

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = listOf(
                NormalizedRule(from = "samplevalue", to = "changedvalue"),
            ),
        ).getOrThrow()

        assertThat(stringsFile.readText()).isEqualTo(
            """
            <resources>
                <string name="changedvalue_title">hello changedvalue</string>
                <string-array name="changedvalue_labels">
                    <item>changedvalue one</item>
                </string-array>
                <item type="string" name="changedvalue_subtitle">changedvalue subtitle</item>
                <color name="bg_redesign_input_changedvalue_black">#000000</color>
            </resources>
            """.trimIndent()
        )
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `transform - rewrites public xml - when resource identifiers contain matching literals`(
        @TempDir dir: File,
    ) {
        val publicXml = dir.resolve("res/values/public.xml").apply {
            parentFile.mkdirs()
            writeText(
                """
                <resources>
                    <public type="drawable" name="ic_logo_samplevalue" id="0x7f010001" />
                </resources>
                """.trimIndent()
            )
        }

        val warnings = transformer.transform(
            workspaceDirectory = dir,
            rules = listOf(
                NormalizedRule(from = "samplevalue", to = "changedvalue"),
            ),
        ).getOrThrow()

        assertThat(publicXml.readText()).isEqualTo(
            """
            <resources>
                <public type="drawable" name="ic_logo_changedvalue" id="0x7f010001" />
            </resources>
            """.trimIndent()
        )
        assertThat(warnings).isEmpty()
    }

    private fun writeTextFile(
        dir: File,
        relativePath: String,
        content: String,
    ): File {
        return dir.resolve(relativePath).also { file ->
            file.parentFile?.mkdirs()
            file.writeText(content)
        }
    }

    private fun writeBinaryFile(
        dir: File,
        relativePath: String,
        content: ByteArray,
    ): File {
        return dir.resolve(relativePath).also { file ->
            file.parentFile?.mkdirs()
            file.writeBytes(content)
        }
    }
}
