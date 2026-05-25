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
import org.jf.dexlib2.iface.instruction.SwitchPayload
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.iface.value.StringEncodedValue
import org.jf.dexlib2.immutable.ImmutableClassDef
import org.jf.dexlib2.immutable.ImmutableDexFile
import org.jf.dexlib2.immutable.ImmutableField
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation
import org.jf.dexlib2.immutable.ImmutableMethodParameter
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21s
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22t
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31i
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31t
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c
import org.jf.dexlib2.immutable.instruction.ImmutablePackedSwitchPayload
import org.jf.dexlib2.immutable.instruction.ImmutableSparseSwitchPayload
import org.jf.dexlib2.immutable.instruction.ImmutableSwitchElement
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference
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
import kotlin.metadata.jvm.JvmMetadataVersion
import kotlin.metadata.jvm.KmModule
import kotlin.metadata.jvm.KmPackageParts
import kotlin.metadata.jvm.KotlinModuleMetadata
import kotlin.metadata.jvm.UnstableMetadataApi

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
            "base/root/META-INF/samplevalue_module.kotlin_module" to createKotlinModuleBytes(),
            "BUNDLE-METADATA/com.android.tools.build.libraries/dependencies.pb" to opaqueProtobufPayload,
            "META-INF/BNDLTOOL.SF" to "signature".toByteArray(StandardCharsets.UTF_8),
            "META-INF/services/demo.Service" to "implementation".toByteArray(StandardCharsets.UTF_8),
        ),
    )
}

@OptIn(UnstableMetadataApi::class)
internal fun createKotlinModuleBytes(token: String = "samplevalue"): ByteArray {
    val packageParts = KmPackageParts(
        fileFacades = mutableListOf("com/example/$token/${token}Kt"),
        multiFileClassParts = mutableMapOf(
            "com/example/$token/${token}Multifile__Part" to "com/example/$token/${token}Multifile",
        ),
    )
    val module = KmModule().apply {
        this.packageParts["com.example.$token"] = packageParts
    }
    return KotlinModuleMetadata(module, JvmMetadataVersion.LATEST_STABLE_SUPPORTED).write()
}

@OptIn(UnstableMetadataApi::class)
internal fun parseKotlinModule(bytes: ByteArray): KmModule {
    return checkNotNull(KotlinModuleMetadata.read(bytes)) {
        "kotlin_module bytes are not parseable"
    }.kmModule
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

/**
 * Synthesises a dex with one method holding the
 * `invoke-virtual String.hashCode → move-result → (sparse|packed)-switch` shape
 * Kotlin emits for `when (String)`. Each case body is `const-string` + `return-void`.
 *
 * Pass [caseKeys] to seed the payload with stale keys (decoupled from [cases]), which
 * reproduces the post-string-rewrite state where keys still hash the original literals.
 */
internal fun createDexWithHashSwitchBytes(
    cases: List<String>,
    sparse: Boolean = true,
    caseKeys: List<Int>? = null,
    classToken: String = "samplevalue",
): ByteArray {
    val classType = "Lcom/example/$classToken/HashSwitchHolder;"
    val hashCodeRef = ImmutableMethodReference(
        "Ljava/lang/String;",
        "hashCode",
        emptyList<String>(),
        "I",
    )

    val instructions = mutableListOf<org.jf.dexlib2.iface.instruction.Instruction>()
    var addr = 0
    val caseTargetAddrs = mutableListOf<Int>()

    instructions += ImmutableInstruction35c(Opcode.INVOKE_VIRTUAL, 1, 0, 0, 0, 0, 0, hashCodeRef)
    addr += 3
    instructions += ImmutableInstruction11x(Opcode.MOVE_RESULT, 1)
    addr += 1
    val switchAddr = addr
    val switchOpcode = if (sparse) Opcode.SPARSE_SWITCH else Opcode.PACKED_SWITCH
    val switchPlaceholderIndex = instructions.size
    // codeOffset is patched in once the payload's absolute address is known below.
    instructions += ImmutableInstruction31t(switchOpcode, 1, 0)
    addr += 3
    instructions += ImmutableInstruction10x(Opcode.RETURN_VOID)
    addr += 1
    for (caseString in cases) {
        caseTargetAddrs += addr
        instructions += ImmutableInstruction21c(Opcode.CONST_STRING, 2, ImmutableStringReference(caseString))
        addr += 2
        instructions += ImmutableInstruction10x(Opcode.RETURN_VOID)
        addr += 1
    }
    // Switch payloads must be aligned to an even code-unit boundary; pad with a nop.
    if (addr % 2 != 0) {
        instructions += ImmutableInstruction10x(Opcode.NOP)
        addr += 1
    }
    val payloadAddr = addr
    instructions[switchPlaceholderIndex] = ImmutableInstruction31t(switchOpcode, 1, payloadAddr - switchAddr)

    val switchElements = caseTargetAddrs.mapIndexed { index, targetAddr ->
        ImmutableSwitchElement(
            caseKeys?.get(index) ?: cases[index].hashCode(),
            targetAddr - switchAddr,
        )
    }.sortedBy { it.key }
    instructions += if (sparse) {
        ImmutableSparseSwitchPayload(switchElements)
    } else {
        ImmutablePackedSwitchPayload(switchElements)
    }

    val method = ImmutableMethod(
        classType,
        "resolve",
        listOf(ImmutableMethodParameter("Ljava/lang/String;", emptySet(), "input")),
        "V",
        AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
        emptySet(),
        ImmutableMethodImplementation(3, instructions, emptyList(), emptyList()),
    )
    val classDef = ImmutableClassDef(
        classType,
        AccessFlags.PUBLIC.value,
        "Ljava/lang/Object;",
        emptyList(),
        "HashSwitchHolder.kt",
        emptySet(),
        emptyList(),
        listOf(method),
    )
    val dexFile = ImmutableDexFile(Opcodes.getDefault(), listOf(classDef))
    val tempFile = Files.createTempFile("hash-switch-fixture-", ".dex").toFile()
    return try {
        DexPool.writeTo(tempFile.path, dexFile)
        tempFile.readBytes()
    } finally {
        tempFile.delete()
    }
}

internal fun DexFile.hashSwitchKeys(): List<Int> {
    val payload = classes.single().methods.single().implementation!!
        .instructions
        .filter { it is SparseSwitchPayload || it is PackedSwitchPayload }
        .single() as SwitchPayload
    return payload.switchElements.map { it.key }
}

internal fun DexFile.hashSwitchCaseStrings(): List<String> =
    classes.single().methods.single().implementation!!
        .instructions
        .filterIsInstance<Instruction21c>()
        .filter { it.opcode == Opcode.CONST_STRING }
        .map { (it.reference as StringReference).string }

/**
 * Synthesises the `const + if` chain R8 emits for small `when (String)` dispatches:
 * `String.hashCode → move-result → (const vK, #key; if-eq vHash, vK, :case)+`. The `const`
 * width mirrors R8 — keys that fit a signed 16-bit value use `const/16`, the rest `const`.
 *
 * With [invertLastCase], the final case is emitted as R8's inverted form
 * (`if-ne vHash, vK, :default` falling through into the case body) instead of `if-eq`.
 *
 * [caseKeys] seeds the compared immediates independently of [cases] to reproduce the stale
 * post-rewrite state. If null, keys are the current hashes (a valid baseline chain).
 */
internal fun createDexWithIfEqChainBytes(
    cases: List<String>,
    caseKeys: List<Int>? = null,
    invertLastCase: Boolean = false,
    classToken: String = "samplevalue",
): ByteArray {
    val classType = "Lcom/example/$classToken/IfEqChainHolder;"
    val hashCodeRef = ImmutableMethodReference("Ljava/lang/String;", "hashCode", emptyList<String>(), "I")

    val keys = cases.indices.map { caseKeys?.get(it) ?: cases[it].hashCode() }
    fun isInverted(i: Int) = invertLastCase && i == cases.lastIndex
    fun constInsn(key: Int) = if (key in Short.MIN_VALUE..Short.MAX_VALUE) {
        ImmutableInstruction21s(Opcode.CONST_16, 2, key)
    } else {
        ImmutableInstruction31i(Opcode.CONST, 2, key)
    }

    // Emit in physical order with placeholder branch offsets, then patch once addresses
    // are known. An inverted (if-ne) case body falls through immediately after its branch;
    // if-eq case bodies live past the default return-void.
    val instructions = mutableListOf<org.jf.dexlib2.iface.instruction.Instruction>()
    val branchIndices = IntArray(cases.size)
    val bodyIndices = IntArray(cases.size)

    instructions += ImmutableInstruction35c(Opcode.INVOKE_VIRTUAL, 1, 0, 0, 0, 0, 0, hashCodeRef)
    instructions += ImmutableInstruction11x(Opcode.MOVE_RESULT, 1)
    keys.forEachIndexed { i, key ->
        instructions += constInsn(key)
        branchIndices[i] = instructions.size
        instructions += ImmutableInstruction22t(if (isInverted(i)) Opcode.IF_NE else Opcode.IF_EQ, 1, 2, 0)
        if (isInverted(i)) {
            bodyIndices[i] = instructions.size
            instructions += ImmutableInstruction21c(Opcode.CONST_STRING, 3, ImmutableStringReference(cases[i]))
            instructions += ImmutableInstruction10x(Opcode.RETURN_VOID)
        }
    }
    val defaultIndex = instructions.size
    instructions += ImmutableInstruction10x(Opcode.RETURN_VOID)
    cases.forEachIndexed { i, caseString ->
        if (isInverted(i)) return@forEachIndexed
        bodyIndices[i] = instructions.size
        instructions += ImmutableInstruction21c(Opcode.CONST_STRING, 3, ImmutableStringReference(caseString))
        instructions += ImmutableInstruction10x(Opcode.RETURN_VOID)
    }

    val addrOf = IntArray(instructions.size)
    var addr = 0
    instructions.forEachIndexed { i, insn -> addrOf[i] = addr; addr += insn.codeUnits }
    keys.forEachIndexed { i, _ ->
        val branchAddr = addrOf[branchIndices[i]]
        val targetIndex = if (isInverted(i)) defaultIndex else bodyIndices[i]
        val opcode = if (isInverted(i)) Opcode.IF_NE else Opcode.IF_EQ
        instructions[branchIndices[i]] =
            ImmutableInstruction22t(opcode, 1, 2, addrOf[targetIndex] - branchAddr)
    }

    val method = ImmutableMethod(
        classType,
        "resolve",
        listOf(ImmutableMethodParameter("Ljava/lang/String;", emptySet(), "input")),
        "V",
        AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
        emptySet(),
        ImmutableMethodImplementation(4, instructions, emptyList(), emptyList()),
    )
    val classDef = ImmutableClassDef(
        classType,
        AccessFlags.PUBLIC.value,
        "Ljava/lang/Object;",
        emptyList(),
        "IfEqChainHolder.kt",
        emptySet(),
        emptyList(),
        listOf(method),
    )
    val dexFile = ImmutableDexFile(Opcodes.getDefault(), listOf(classDef))
    val tempFile = Files.createTempFile("if-eq-chain-fixture-", ".dex").toFile()
    return try {
        DexPool.writeTo(tempFile.path, dexFile)
        tempFile.readBytes()
    } finally {
        tempFile.delete()
    }
}

/** Compared-key immediates of the if-eq chain, in instruction order. */
internal fun DexFile.ifEqChainKeys(): List<Int> =
    classes.single().methods.single().implementation!!
        .instructions
        .filter { it.opcode == Opcode.CONST || it.opcode == Opcode.CONST_16 }
        .map { (it as org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction).narrowLiteral }
