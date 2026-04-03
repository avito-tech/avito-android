package com.avito.android.string_transform

import com.android.aapt.Resources
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.protobuf.TextFormat
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.iface.value.StringEncodedValue
import org.jf.dexlib2.immutable.ImmutableClassDef
import org.jf.dexlib2.immutable.ImmutableDexFile
import org.jf.dexlib2.immutable.ImmutableField
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation
import org.jf.dexlib2.immutable.ImmutableMethodParameter
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c
import org.jf.dexlib2.immutable.reference.ImmutableStringReference
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue
import org.jf.dexlib2.writer.pool.DexPool
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal fun sampleRule(): List<NormalizedRule> {
    return listOf(NormalizedRule(from = "samplevalue", to = "changedvalue"))
}

internal fun createAabFixture(archive: File) {
    // Arbitrary protobuf-like binary payloads used to verify that unsupported *.pb files survive roundtrip unchanged.
    val opaqueProtobufPayload = byteArrayOf(0x0A, 0x06, 0x6F, 0x70, 0x61, 0x71, 0x75, 0x65)

    createZip(
        archive = archive,
        entries = mapOf(
            "BundleConfig.pb" to opaqueProtobufPayload,
            "base/resources.pb" to createResourcesPbBytes(),
            "base/native.pb" to opaqueProtobufPayload,
            "base/manifest/AndroidManifest.xml" to createProtobufXmlBytes(),
            "base/res/xml/samplevalue_config.xml" to createProtobufXmlBytes(),
            "base/res/raw/samplevalue.xml" to "samplevalue".toByteArray(StandardCharsets.UTF_8),
            "base/assets/samplevalue.txt" to "samplevalue".toByteArray(StandardCharsets.UTF_8),
            "base/dex/classes.dex" to createDexBytes(),
            "META-INF/BNDLTOOL.SF" to "signature".toByteArray(StandardCharsets.UTF_8),
            "META-INF/services/demo.Service" to "implementation".toByteArray(StandardCharsets.UTF_8),
        ),
    )
}

internal fun createResourcesPbBytes(token: String = "samplevalue"): ByteArray {
    val rawString = Resources.RawString.newBuilder()
        .setValue(token)
        .build()
    val item = Resources.Item.newBuilder()
        .setRawStr(rawString)
        .build()
    val value = Resources.Value.newBuilder()
        .setItem(item)
        .build()
    val configValue = Resources.ConfigValue.newBuilder()
        .setValue(value)
        .build()
    val entry = Resources.Entry.newBuilder()
        .setName("${token}_entry")
        .addConfigValue(configValue)
        .build()
    val type = Resources.Type.newBuilder()
        .setName("${token}_type")
        .addEntry(entry)
        .build()
    val pkg = Resources.Package.newBuilder()
        .setPackageName("com.example.$token")
        .addType(type)
        .build()

    return Resources.ResourceTable.newBuilder()
        .addPackage(pkg)
        .build()
        .toByteArray()
}

internal fun createProtobufXmlBytes(token: String = "samplevalue"): ByteArray {
    val childNode = Resources.XmlNode.newBuilder()
        .setText(token)
        .build()
    val attribute = Resources.XmlAttribute.newBuilder()
        .setName("${token}_attr")
        .setValue(token)
        .build()
    val element = Resources.XmlElement.newBuilder()
        .setName("${token}_node")
        .addAttribute(attribute)
        .addChild(childNode)
        .build()

    return Resources.XmlNode.newBuilder()
        .setElement(element)
        .build()
        .toByteArray()
}

internal fun createDexBytes(token: String = "samplevalue"): ByteArray {
    val classType = "Lcom/example/$token/Holder;"
    val method = ImmutableMethod(
        classType,
        "${token}Method",
        listOf(ImmutableMethodParameter("Ljava/lang/String;", emptySet(), "${token}Param")),
        "V",
        AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
        emptySet(),
        ImmutableMethodImplementation(
            1,
            listOf(
                ImmutableInstruction21c(
                    Opcode.CONST_STRING,
                    0,
                    ImmutableStringReference(token),
                ),
                ImmutableInstruction10x(Opcode.RETURN_VOID),
            ),
            emptyList(),
            emptyList(),
        ),
    )
    val field = ImmutableField(
        classType,
        "${token}Field",
        "Ljava/lang/String;",
        AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
        ImmutableStringEncodedValue(token),
        emptySet(),
    )
    val classDef = ImmutableClassDef(
        classType,
        AccessFlags.PUBLIC.value,
        "Ljava/lang/Object;",
        emptyList(),
        "$token.kt",
        emptySet(),
        listOf(field),
        listOf(method),
    )
    val dexFile = ImmutableDexFile(Opcodes.getDefault(), listOf(classDef))
    val tempFile = Files.createTempFile("aab-fixture-", ".dex").toFile()

    return try {
        DexPool.writeTo(tempFile.path, dexFile)
        tempFile.readBytes()
    } finally {
        tempFile.delete()
    }
}

internal fun createZip(archive: File, entries: Map<String, ByteArray>) {
    archive.parentFile.mkdirs()
    ZipOutputStream(FileOutputStream(archive)).use { zip ->
        entries.forEach { (path, content) ->
            zip.putNextEntry(ZipEntry(path))
            zip.write(content)
            zip.closeEntry()
        }
    }
}

internal fun ZipFile.readEntryBytes(path: String): ByteArray {
    val entry = checkNotNull(getEntry(path)) {
        "Zip entry not found: $path"
    }
    return getInputStream(entry).use { it.readBytes() }
}

internal fun ZipFile.readEntryText(path: String): String {
    return readEntryBytes(path).toString(StandardCharsets.UTF_8)
}

internal fun Resources.ResourceTable.asTextFormat(): String {
    return TextFormat.printer().printToString(this)
}

internal fun Resources.XmlNode.asTextFormat(): String {
    return TextFormat.printer().printToString(this)
}

internal fun readDexFile(bytes: ByteArray, tempDirectory: File, name: String = "fixture.dex"): DexFile {
    val file = tempDirectory.resolve(name)
    file.parentFile.mkdirs()
    file.writeBytes(bytes)
    return DexFileFactory.loadDexFile(file, null)
}

internal fun DexFile.primaryClassType(): String {
    return classes.single().type
}

internal fun DexFile.primaryClassSourceFile(): String? {
    return classes.single().sourceFile
}

internal fun DexFile.primaryFieldName(): String {
    return classes.single().fields.single().name
}

internal fun DexFile.primaryFieldInitialStringValue(): String {
    return (classes.single().fields.single().initialValue as StringEncodedValue).value
}

internal fun DexFile.primaryMethodName(): String {
    return classes.single().methods.single().name
}

internal fun DexFile.primaryMethodParameterName(): String? {
    return classes.single().methods.single().parameters.single().name
}

internal fun DexFile.primaryMethodConstString(): String {
    val instruction = classes.single().methods.single().implementation!!
        .instructions
        .filterIsInstance<ReferenceInstruction>()
        .single()
    return (instruction.reference as StringReference).string
}
