package com.avito.android.string_transform.internal.task.common

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.apk.OperationWarning
import org.jf.dexlib2.DebugItemType
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.ReferenceType
import org.jf.dexlib2.ValueType
import org.jf.dexlib2.base.BaseAnnotationElement
import org.jf.dexlib2.base.BaseMethodParameter
import org.jf.dexlib2.base.reference.BaseFieldReference
import org.jf.dexlib2.base.reference.BaseMethodReference
import org.jf.dexlib2.base.reference.BaseStringReference
import org.jf.dexlib2.base.reference.BaseTypeReference
import org.jf.dexlib2.base.value.BaseStringEncodedValue
import org.jf.dexlib2.iface.AnnotationElement
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.MethodParameter
import org.jf.dexlib2.iface.debug.DebugItem
import org.jf.dexlib2.iface.debug.EndLocal
import org.jf.dexlib2.iface.debug.LocalInfo
import org.jf.dexlib2.iface.debug.RestartLocal
import org.jf.dexlib2.iface.debug.SetSourceFile
import org.jf.dexlib2.iface.debug.StartLocal
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction20bc
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction22c
import org.jf.dexlib2.iface.instruction.formats.Instruction31c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.Reference
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.iface.reference.TypeReference
import org.jf.dexlib2.iface.value.EncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue
import org.jf.dexlib2.rewriter.AnnotationElementRewriter
import org.jf.dexlib2.rewriter.ClassDefRewriter
import org.jf.dexlib2.rewriter.DebugItemRewriter
import org.jf.dexlib2.rewriter.DexRewriter
import org.jf.dexlib2.rewriter.EncodedValueRewriter
import org.jf.dexlib2.rewriter.FieldReferenceRewriter
import org.jf.dexlib2.rewriter.InstructionRewriter
import org.jf.dexlib2.rewriter.MethodParameterRewriter
import org.jf.dexlib2.rewriter.MethodReferenceRewriter
import org.jf.dexlib2.rewriter.Rewriter
import org.jf.dexlib2.rewriter.RewriterModule
import org.jf.dexlib2.rewriter.RewriterUtils
import org.jf.dexlib2.rewriter.Rewriters
import org.jf.dexlib2.writer.pool.DexPool
import java.io.File

internal class DexTransformer {

    fun transform(
        inputFile: File,
        rules: List<NormalizedRule>,
        affectedPath: String = inputFile.invariantSeparatorsPath,
    ): Result<List<OperationWarning>> = Result.tryCatch {
        val transform = RuleStringTransform(rules)
        val dexFile = DexFileFactory.loadDexFile(inputFile, null)
        val rewrittenDexFile = DexRewriter(DexRuleRewriterModule(transform))
            .rewriteDexFile(dexFile)

        DexPool.writeTo(inputFile.path, rewrittenDexFile)
        val pendingWarnings = dexPendingLiteralWarnings(
            outputBytes = inputFile.readBytes(),
            rules = rules,
            affectedPath = affectedPath,
        )

        val recomputeWarnings = if (rules.isNotEmpty()) {
            HashSwitchKeyRecomputer().recompute(inputFile, affectedPath).getOrThrow()
        } else {
            emptyList()
        }

        pendingWarnings + recomputeWarnings
    }

    private class DexRuleRewriterModule(
        private val transform: RuleStringTransform,
    ) : RewriterModule() {

        override fun getTypeRewriter(rewriters: Rewriters): Rewriter<String> {
            return Rewriter { value -> transform.apply(value) }
        }

        override fun getFieldReferenceRewriter(rewriters: Rewriters): Rewriter<FieldReference> {
            return object : FieldReferenceRewriter(rewriters) {
                override fun rewrite(fieldReference: FieldReference): FieldReference {
                    return object : BaseFieldReference() {
                        override fun getDefiningClass(): String {
                            return rewriters.typeRewriter.rewrite(fieldReference.definingClass)
                        }

                        override fun getName(): String {
                            return transform.apply(fieldReference.name)
                        }

                        override fun getType(): String {
                            return rewriters.typeRewriter.rewrite(fieldReference.type)
                        }
                    }
                }
            }
        }

        override fun getMethodReferenceRewriter(rewriters: Rewriters): Rewriter<MethodReference> {
            return object : MethodReferenceRewriter(rewriters) {
                override fun rewrite(methodReference: MethodReference): MethodReference {
                    return object : BaseMethodReference() {
                        override fun getDefiningClass(): String {
                            return rewriters.typeRewriter.rewrite(methodReference.definingClass)
                        }

                        override fun getName(): String {
                            return transform.apply(methodReference.name)
                        }

                        override fun getParameterTypes(): List<String> {
                            return methodReference.parameterTypes.map { parameterType ->
                                rewriters.typeRewriter.rewrite(parameterType.toString())
                            }
                        }

                        override fun getReturnType(): String {
                            return rewriters.typeRewriter.rewrite(methodReference.returnType)
                        }
                    }
                }
            }
        }

        override fun getMethodParameterRewriter(rewriters: Rewriters): Rewriter<MethodParameter> {
            return object : MethodParameterRewriter(rewriters) {
                override fun rewrite(methodParameter: MethodParameter): MethodParameter {
                    return object : BaseMethodParameter() {
                        override fun getType(): String {
                            return rewriters.typeRewriter.rewrite(methodParameter.type)
                        }

                        override fun getAnnotations() = RewriterUtils.rewriteSet(
                            rewriters.annotationRewriter,
                            methodParameter.annotations,
                        )

                        override fun getName(): String? {
                            return methodParameter.name?.let(transform::apply)
                        }

                        override fun getSignature(): String? {
                            return methodParameter.signature?.let(transform::apply)
                        }
                    }
                }
            }
        }

        override fun getClassDefRewriter(rewriters: Rewriters): Rewriter<ClassDef> {
            return object : ClassDefRewriter(rewriters) {
                override fun rewrite(classDef: ClassDef): ClassDef {
                    return object : BaseTypeReference(), ClassDef {
                        override fun getType(): String {
                            return rewriters.typeRewriter.rewrite(classDef.type)
                        }

                        override fun getAccessFlags(): Int = classDef.accessFlags

                        override fun getSuperclass(): String? {
                            return classDef.superclass?.let(rewriters.typeRewriter::rewrite)
                        }

                        override fun getInterfaces(): List<String> {
                            return RewriterUtils.rewriteList(rewriters.typeRewriter, classDef.interfaces)
                        }

                        override fun getSourceFile(): String? {
                            return classDef.sourceFile?.let(transform::apply)
                        }

                        override fun getAnnotations() = RewriterUtils.rewriteSet(
                            rewriters.annotationRewriter,
                            classDef.annotations,
                        )

                        override fun getStaticFields() = RewriterUtils.rewriteIterable(
                            rewriters.fieldRewriter,
                            classDef.staticFields,
                        )

                        override fun getInstanceFields() = RewriterUtils.rewriteIterable(
                            rewriters.fieldRewriter,
                            classDef.instanceFields,
                        )

                        override fun getFields() = RewriterUtils.rewriteIterable(
                            rewriters.fieldRewriter,
                            classDef.fields,
                        )

                        override fun getDirectMethods() = RewriterUtils.rewriteIterable(
                            rewriters.methodRewriter,
                            classDef.directMethods,
                        )

                        override fun getVirtualMethods() = RewriterUtils.rewriteIterable(
                            rewriters.methodRewriter,
                            classDef.virtualMethods,
                        )

                        override fun getMethods() = RewriterUtils.rewriteIterable(
                            rewriters.methodRewriter,
                            classDef.methods,
                        )
                    }
                }
            }
        }

        override fun getInstructionRewriter(rewriters: Rewriters): Rewriter<Instruction> {
            return object : InstructionRewriter(rewriters) {
                override fun rewrite(instruction: Instruction): Instruction {
                    if (instruction !is ReferenceInstruction) {
                        return instruction
                    }

                    return when (instruction.opcode.format) {
                        org.jf.dexlib2.Format.Format20bc -> RewrittenInstruction20bc(instruction as Instruction20bc)
                        org.jf.dexlib2.Format.Format21c -> RewrittenInstruction21c(instruction as Instruction21c)
                        org.jf.dexlib2.Format.Format22c -> RewrittenInstruction22c(instruction as Instruction22c)
                        org.jf.dexlib2.Format.Format31c -> RewrittenInstruction31c(instruction as Instruction31c)
                        org.jf.dexlib2.Format.Format35c -> RewrittenInstruction35c(instruction as Instruction35c)
                        org.jf.dexlib2.Format.Format3rc -> RewrittenInstruction3rc(instruction as Instruction3rc)
                        else -> instruction
                    }
                }

                private fun rewriteReference(instruction: ReferenceInstruction): Reference {
                    return when (instruction.referenceType) {
                        ReferenceType.TYPE -> RewriterUtils.rewriteTypeReference(
                            rewriters.typeRewriter,
                            instruction.reference as TypeReference,
                        )

                        ReferenceType.FIELD -> rewriters.fieldReferenceRewriter
                            .rewrite(instruction.reference as FieldReference)

                        ReferenceType.METHOD -> rewriters.methodReferenceRewriter
                            .rewrite(instruction.reference as MethodReference)

                        ReferenceType.STRING -> rewrittenStringReference(instruction.reference as StringReference)

                        else -> instruction.reference
                    }
                }

                private fun rewrittenStringReference(reference: StringReference): StringReference {
                    return stringReference(transform.apply(reference.string))
                }

                inner class RewrittenInstruction20bc(
                    private val instruction: Instruction20bc,
                ) : Instruction20bc {
                    override fun getReference(): Reference = rewriteReference(instruction)
                    override fun getReferenceType(): Int = instruction.referenceType
                    override fun getOpcode() = instruction.opcode
                    override fun getCodeUnits(): Int = instruction.codeUnits
                    override fun getVerificationError(): Int = instruction.verificationError
                }

                inner class RewrittenInstruction21c(
                    private val instruction: Instruction21c,
                ) : Instruction21c {
                    override fun getReference(): Reference = rewriteReference(instruction)
                    override fun getReferenceType(): Int = instruction.referenceType
                    override fun getOpcode() = instruction.opcode
                    override fun getCodeUnits(): Int = instruction.codeUnits
                    override fun getRegisterA(): Int = instruction.registerA
                }

                inner class RewrittenInstruction22c(
                    private val instruction: Instruction22c,
                ) : Instruction22c {
                    override fun getReference(): Reference = rewriteReference(instruction)
                    override fun getReferenceType(): Int = instruction.referenceType
                    override fun getOpcode() = instruction.opcode
                    override fun getCodeUnits(): Int = instruction.codeUnits
                    override fun getRegisterA(): Int = instruction.registerA
                    override fun getRegisterB(): Int = instruction.registerB
                }

                inner class RewrittenInstruction31c(
                    private val instruction: Instruction31c,
                ) : Instruction31c {
                    override fun getReference(): Reference = rewriteReference(instruction)
                    override fun getReferenceType(): Int = instruction.referenceType
                    override fun getOpcode() = instruction.opcode
                    override fun getCodeUnits(): Int = instruction.codeUnits
                    override fun getRegisterA(): Int = instruction.registerA
                }

                inner class RewrittenInstruction35c(
                    private val instruction: Instruction35c,
                ) : Instruction35c {
                    override fun getReference(): Reference = rewriteReference(instruction)
                    override fun getReferenceType(): Int = instruction.referenceType
                    override fun getOpcode() = instruction.opcode
                    override fun getCodeUnits(): Int = instruction.codeUnits
                    override fun getRegisterCount(): Int = instruction.registerCount
                    override fun getRegisterC(): Int = instruction.registerC
                    override fun getRegisterD(): Int = instruction.registerD
                    override fun getRegisterE(): Int = instruction.registerE
                    override fun getRegisterF(): Int = instruction.registerF
                    override fun getRegisterG(): Int = instruction.registerG
                }

                inner class RewrittenInstruction3rc(
                    private val instruction: Instruction3rc,
                ) : Instruction3rc {
                    override fun getReference(): Reference = rewriteReference(instruction)
                    override fun getReferenceType(): Int = instruction.referenceType
                    override fun getOpcode() = instruction.opcode
                    override fun getCodeUnits(): Int = instruction.codeUnits
                    override fun getStartRegister(): Int = instruction.startRegister
                    override fun getRegisterCount(): Int = instruction.registerCount
                }
            }
        }

        override fun getDebugItemRewriter(rewriters: Rewriters): Rewriter<DebugItem> {
            return object : DebugItemRewriter(rewriters) {
                override fun rewrite(value: DebugItem): DebugItem {
                    return when (value.debugItemType) {
                        DebugItemType.START_LOCAL -> rewrittenLocalInfo(value as StartLocal)
                        DebugItemType.END_LOCAL -> rewrittenLocalInfo(value as EndLocal)
                        DebugItemType.RESTART_LOCAL -> rewrittenLocalInfo(value as RestartLocal)
                        DebugItemType.SET_SOURCE_FILE -> rewrittenSetSourceFile(value as SetSourceFile)
                        else -> value
                    }
                }

                private fun rewrittenSetSourceFile(item: SetSourceFile): SetSourceFile {
                    return object : SetSourceFile {
                        override fun getDebugItemType(): Int = item.debugItemType
                        override fun getCodeAddress(): Int = item.codeAddress
                        override fun getSourceFile(): String? = item.sourceFile?.let(transform::apply)
                        override fun getSourceFileReference(): StringReference? {
                            return item.sourceFileReference?.let { stringReference(transform.apply(it.string)) }
                        }
                    }
                }

                private fun <T> rewrittenLocalInfo(item: T): T where T : DebugItem, T : LocalInfo {
                    @Suppress("UNCHECKED_CAST")
                    return when (item) {
                        is StartLocal -> object : StartLocal {
                            override fun getDebugItemType(): Int = item.debugItemType
                            override fun getCodeAddress(): Int = item.codeAddress
                            override fun getRegister(): Int = item.register
                            override fun getName(): String? = item.name?.let(transform::apply)
                            override fun getType(): String? = item.type?.let(rewriters.typeRewriter::rewrite)
                            override fun getSignature(): String? = item.signature?.let(transform::apply)
                            override fun getNameReference(): StringReference? {
                                return item.nameReference?.let { stringReference(transform.apply(it.string)) }
                            }

                            override fun getTypeReference(): TypeReference? {
                                return item.typeReference?.let {
                                    RewriterUtils.rewriteTypeReference(rewriters.typeRewriter, it)
                                }
                            }

                            override fun getSignatureReference(): StringReference? {
                                return item.signatureReference?.let { stringReference(transform.apply(it.string)) }
                            }
                        } as T

                        is EndLocal -> object : EndLocal {
                            override fun getDebugItemType(): Int = item.debugItemType
                            override fun getCodeAddress(): Int = item.codeAddress
                            override fun getRegister(): Int = item.register
                            override fun getName(): String? = item.name?.let(transform::apply)
                            override fun getType(): String? = item.type?.let(rewriters.typeRewriter::rewrite)
                            override fun getSignature(): String? = item.signature?.let(transform::apply)
                        } as T

                        is RestartLocal -> object : RestartLocal {
                            override fun getDebugItemType(): Int = item.debugItemType
                            override fun getCodeAddress(): Int = item.codeAddress
                            override fun getRegister(): Int = item.register
                            override fun getName(): String? = item.name?.let(transform::apply)
                            override fun getType(): String? = item.type?.let(rewriters.typeRewriter::rewrite)
                            override fun getSignature(): String? = item.signature?.let(transform::apply)
                        } as T

                        else -> item
                    }
                }
            }
        }

        override fun getEncodedValueRewriter(rewriters: Rewriters): Rewriter<EncodedValue> {
            return object : EncodedValueRewriter(rewriters) {
                override fun rewrite(encodedValue: EncodedValue): EncodedValue {
                    return when (encodedValue.valueType) {
                        ValueType.STRING -> rewrittenStringEncodedValue(encodedValue as StringEncodedValue)
                        else -> super.rewrite(encodedValue)
                    }
                }

                private fun rewrittenStringEncodedValue(value: StringEncodedValue): EncodedValue {
                    return object : BaseStringEncodedValue() {
                        override fun getValue(): String = transform.apply(value.value)
                    }
                }
            }
        }

        override fun getAnnotationElementRewriter(rewriters: Rewriters): Rewriter<AnnotationElement> {
            return object : AnnotationElementRewriter(rewriters) {
                override fun rewrite(annotationElement: AnnotationElement): AnnotationElement {
                    return object : BaseAnnotationElement() {
                        override fun getName(): String = transform.apply(annotationElement.name)
                        override fun getValue(): EncodedValue {
                            return rewriters.encodedValueRewriter.rewrite(annotationElement.value)
                        }
                    }
                }
            }
        }

        private fun stringReference(value: String): StringReference {
            return object : BaseStringReference() {
                override fun getString(): String = value
            }
        }
    }

    private class RuleStringTransform(
        private val rules: List<NormalizedRule>,
    ) {
        fun apply(value: String): String {
            return rules.fold(value) { current, rule ->
                current.replace(rule.from, rule.to)
            }
        }
    }
}
