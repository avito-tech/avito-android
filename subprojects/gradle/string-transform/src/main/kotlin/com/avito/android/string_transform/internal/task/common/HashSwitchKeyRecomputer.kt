package com.avito.android.string_transform.internal.task.common

import com.avito.android.Result
import com.avito.android.string_transform.internal.task.apk.OperationWarning
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.MethodImplementation
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.SwitchElement
import org.jf.dexlib2.iface.instruction.ThreeRegisterInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction22t
import org.jf.dexlib2.iface.instruction.formats.Instruction31t
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11n
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21ih
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21s
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31i
import org.jf.dexlib2.immutable.instruction.ImmutablePackedSwitchPayload
import org.jf.dexlib2.immutable.instruction.ImmutableSparseSwitchPayload
import org.jf.dexlib2.immutable.instruction.ImmutableSwitchElement
import org.jf.dexlib2.rewriter.DexRewriter
import org.jf.dexlib2.rewriter.Rewriter
import org.jf.dexlib2.rewriter.RewriterModule
import org.jf.dexlib2.rewriter.Rewriters
import org.jf.dexlib2.writer.pool.DexPool
import java.io.File

/**
 * Rehashes case keys of `when (String)` dispatches after the string-pool rewriter has
 * replaced the literals. Kotlin lowers such `when`s to `switch (str.hashCode())` with
 * the hashes baked in at compile time, so without a second pass the switch falls into
 * `else` for every case whose string changed.
 *
 * Covers the codegen variants we observe in practice:
 *   - `sparse-switch` / `packed-switch` payloads (kotlinc default for ≥ 4 cases);
 *   - `const{,/16,/4,/high16} + if-eq/if-ne` chains (R8 lowering for small case sets,
 *     where R8 inverts the final case to `if-ne <default>` and falls through to its body);
 *   - `const-string/jumbo` case-body targets (large string pools).
 */
internal class HashSwitchKeyRecomputer {

    fun recompute(
        inputFile: File,
        affectedPath: String = inputFile.invariantSeparatorsPath,
    ): Result<List<OperationWarning>> = Result.tryCatch {
        val collector = WarningCollector(affectedPath)
        val dexFile = DexFileFactory.loadDexFile(inputFile, null)
        val rewritten = DexRewriter(RecomputeModule(collector)).rewriteDexFile(dexFile)
        DexPool.writeTo(inputFile.path, rewritten)
        collector.warnings.toList()
    }

    private class RecomputeModule(
        private val collector: WarningCollector,
    ) : RewriterModule() {
        override fun getMethodImplementationRewriter(rewriters: Rewriters): Rewriter<MethodImplementation> =
            Rewriter { impl -> RecomputedMethodImplementation(impl, collector) }
    }

    private class RecomputedMethodImplementation(
        private val original: MethodImplementation,
        private val collector: WarningCollector,
    ) : MethodImplementation by original {

        // dexlib2 reads the implementation more than once during writing; memoise so
        // detection runs once and warnings don't surface multiple times per method.
        private val rewritten: List<Instruction> by lazy {
            val list = original.instructions.toList()
            val hashSwitches = findHashSwitches(list)
            val ifChainConsts = findIfChainConstReplacements(list, collector)
            if (hashSwitches.isEmpty() && ifChainConsts.isEmpty()) {
                list
            } else {
                rewriteHashSwitchPayloads(list, hashSwitches, collector)
                    .mapIndexed { i, insn -> ifChainConsts[i] ?: insn }
            }
        }

        override fun getInstructions(): Iterable<Instruction> = rewritten
    }

    private data class HashSwitchInfo(val payloadAddr: Int, val switchAddr: Int)

    /**
     * Deduplicates warnings by message: dexlib2 re-reads each [MethodImplementation] during
     * sizing and emit passes, and our wrapper rebuilds detection state each time.
     */
    private class WarningCollector(val affectedPath: String) {
        private val seen = linkedSetOf<String>()
        val warnings = mutableListOf<OperationWarning>()
        fun warn(message: String) {
            if (seen.add(message)) {
                warnings += OperationWarning(message = message, affectedPath = affectedPath)
            }
        }
    }

    private companion object {

        /** Bounded backward scan: enough to clear R8's typical hoisting, cheap, stays in-block. */
        private const val PRIOR_WRITER_WINDOW = 8

        fun findHashSwitches(instructions: List<Instruction>): List<HashSwitchInfo> {
            val result = mutableListOf<HashSwitchInfo>()
            var addr = 0
            instructions.forEachIndexed { i, insn ->
                val switchAddr = addr
                addr += insn.codeUnits
                if (insn !is Instruction31t) return@forEachIndexed
                if (insn.opcode != Opcode.SPARSE_SWITCH && insn.opcode != Opcode.PACKED_SWITCH) return@forEachIndexed
                if (!precededByStringHashCode(instructions, i, insn.registerA)) return@forEachIndexed
                result += HashSwitchInfo(payloadAddr = switchAddr + insn.codeOffset, switchAddr = switchAddr)
            }
            return result
        }

        /**
         * R8 frequently hoists unrelated `const`s between the `move-result` of `String.hashCode`
         * and the dispatch, so we walk backward to the writer of the switch's input register
         * rather than relying on tight adjacency.
         */
        private fun precededByStringHashCode(
            instructions: List<Instruction>,
            switchIndex: Int,
            switchInputRegister: Int,
        ): Boolean {
            val moveResultIndex = findPriorWriterOf(instructions, switchIndex, switchInputRegister) ?: return false
            if (instructions[moveResultIndex].opcode != Opcode.MOVE_RESULT) return false
            val invoke = instructions.getOrNull(moveResultIndex - 1) as? Instruction35c ?: return false
            return invoke.isStringHashCode()
        }

        private fun Instruction35c.isStringHashCode(): Boolean {
            if (opcode != Opcode.INVOKE_VIRTUAL) return false
            val ref = reference as? MethodReference ?: return false
            return ref.definingClass == "Ljava/lang/String;" &&
                ref.name == "hashCode" &&
                ref.returnType == "I" &&
                ref.parameterTypes.isEmpty()
        }

        private fun findPriorWriterOf(instructions: List<Instruction>, fromIndex: Int, reg: Int): Int? {
            val lower = (fromIndex - PRIOR_WRITER_WINDOW).coerceAtLeast(0)
            for (i in fromIndex - 1 downTo lower) {
                if (writesRegister(instructions[i], reg)) return i
            }
            return null
        }

        private fun writesRegister(insn: Instruction, reg: Int): Boolean = when (insn) {
            is OneRegisterInstruction -> insn.registerA == reg
            is TwoRegisterInstruction -> insn.registerA == reg
            is ThreeRegisterInstruction -> insn.registerA == reg
            else -> false
        }

        /**
         * R8 lowers small `when (String)` dispatches into a flat `const + if` chain instead
         * of a switch payload. Returns instruction-index → replacement `const` for every cell
         * whose immediate needs updating.
         */
        fun findIfChainConstReplacements(
            instructions: List<Instruction>,
            collector: WarningCollector,
        ): Map<Int, Instruction> {
            val replacements = mutableMapOf<Int, Instruction>()
            val (addrToIndex, indexToAddr) = computeAddressTables(instructions)

            instructions.forEachIndexed { invokeIdx, invoke ->
                if (invoke !is Instruction35c || !invoke.isStringHashCode()) return@forEachIndexed
                val moveResult = instructions.getOrNull(invokeIdx + 1) ?: return@forEachIndexed
                if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed
                val hashReg = (moveResult as? OneRegisterInstruction)?.registerA ?: return@forEachIndexed
                walkHashCompareChain(
                    instructions, addrToIndex, indexToAddr, invokeIdx + 2, hashReg, replacements, collector,
                )
            }
            return replacements
        }

        private fun computeAddressTables(instructions: List<Instruction>): Pair<Map<Int, Int>, IntArray> {
            val addrToIndex = mutableMapOf<Int, Int>()
            val indexToAddr = IntArray(instructions.size)
            var addr = 0
            instructions.forEachIndexed { i, insn ->
                addrToIndex[addr] = i
                indexToAddr[i] = addr
                addr += insn.codeUnits
            }
            return addrToIndex to indexToAddr
        }

        private fun walkHashCompareChain(
            instructions: List<Instruction>,
            addrToIndex: Map<Int, Int>,
            indexToAddr: IntArray,
            startIndex: Int,
            hashReg: Int,
            replacements: MutableMap<Int, Instruction>,
            collector: WarningCollector,
        ) {
            var i = startIndex
            while (i + 1 < instructions.size) {
                val constInsn = instructions[i]
                if (!constInsn.opcode.isNarrowConst()) return
                val constReg = (constInsn as OneRegisterInstruction).registerA
                val branch = instructions[i + 1] as? Instruction22t ?: return
                if (!comparesHashToConst(branch, hashReg, constReg)) return

                // if-eq jumps to the case body; if-ne (R8's inverted last case) jumps to the
                // default and falls through into the case body at the next instruction.
                val caseIdx = when (branch.opcode) {
                    Opcode.IF_EQ -> addrToIndex[indexToAddr[i + 1] + branch.codeOffset]
                    Opcode.IF_NE -> i + 2
                    else -> return
                }
                val caseString = caseIdx?.let { firstCaseString(instructions, it) }
                if (caseString != null) {
                    rehashConstKey(constInsn, constReg, caseString.hashCode(), i, replacements, collector)
                }
                // An if-ne case body is the fall-through, so the dispatch chain ends here.
                if (branch.opcode == Opcode.IF_NE) return
                i += 2
            }
        }

        /**
         * R8 sizes the immediate to the smallest `const` form that fits, so a case key can be
         * loaded by `const` (32-bit), `const/16`, `const/4` or `const/high16`. We must keep
         * walking past every form, otherwise a narrow non-brand case would mask a later
         * brand case in the same chain.
         */
        private fun Opcode.isNarrowConst(): Boolean =
            this == Opcode.CONST || this == Opcode.CONST_16 || this == Opcode.CONST_4 || this == Opcode.CONST_HIGH16

        private fun rehashConstKey(
            constInsn: Instruction,
            reg: Int,
            newKey: Int,
            index: Int,
            replacements: MutableMap<Int, Instruction>,
            collector: WarningCollector,
        ) {
            if ((constInsn as NarrowLiteralInstruction).narrowLiteral == newKey) return
            val replacement = narrowConstOfSameWidth(constInsn.opcode, reg, newKey)
            if (replacement != null) {
                replacements[index] = replacement
            } else {
                // Widening the const to hold a larger key would grow the instruction and shift
                // every following offset — out of scope. In practice unreachable: brand tokens
                // are ≥ 4 chars, so a rehashed brand key never shrinks below the 32-bit `const`.
                collector.warn(
                    "hash-switch if-eq chain: key $newKey does not fit ${constInsn.opcode}; left unchanged"
                )
            }
        }

        private fun narrowConstOfSameWidth(opcode: Opcode, reg: Int, value: Int): Instruction? = when {
            opcode == Opcode.CONST -> ImmutableInstruction31i(Opcode.CONST, reg, value)
            opcode == Opcode.CONST_16 && value in Short.MIN_VALUE..Short.MAX_VALUE ->
                ImmutableInstruction21s(Opcode.CONST_16, reg, value)
            opcode == Opcode.CONST_4 && value in -8..7 ->
                ImmutableInstruction11n(Opcode.CONST_4, reg, value)
            opcode == Opcode.CONST_HIGH16 && value and 0xFFFF == 0 ->
                ImmutableInstruction21ih(Opcode.CONST_HIGH16, reg, value)
            else -> null
        }

        private fun comparesHashToConst(branch: Instruction22t, hashReg: Int, constReg: Int): Boolean =
            branch.registerA == hashReg && branch.registerB == constReg ||
                branch.registerA == constReg && branch.registerB == hashReg

        fun rewriteHashSwitchPayloads(
            instructions: List<Instruction>,
            hashSwitches: List<HashSwitchInfo>,
            collector: WarningCollector,
        ): List<Instruction> {
            if (hashSwitches.isEmpty()) return instructions
            val payloadAddrToSwitchAddr = hashSwitches.associate { it.payloadAddr to it.switchAddr }
            val (addrToIndex, _) = computeAddressTables(instructions)

            var addr = 0
            return instructions.map { insn ->
                val curAddr = addr
                addr += insn.codeUnits
                val switchAddr = payloadAddrToSwitchAddr[curAddr] ?: return@map insn
                when (insn) {
                    is SparseSwitchPayload -> rewriteSparseSwitch(insn, switchAddr, instructions, addrToIndex)
                    is PackedSwitchPayload ->
                        rewritePackedSwitch(insn, switchAddr, instructions, addrToIndex, collector)
                    else -> insn
                }
            }
        }

        private fun rewriteSparseSwitch(
            payload: SparseSwitchPayload,
            switchAddr: Int,
            instructions: List<Instruction>,
            addrToIndex: Map<Int, Int>,
        ): Instruction {
            val newElements = recomputedElements(payload.switchElements, switchAddr, instructions, addrToIndex)
            return ImmutableSparseSwitchPayload(newElements.sortedBy { it.key })
        }

        private fun rewritePackedSwitch(
            payload: PackedSwitchPayload,
            switchAddr: Int,
            instructions: List<Instruction>,
            addrToIndex: Map<Int, Int>,
            collector: WarningCollector,
        ): Instruction {
            val newElements = recomputedElements(payload.switchElements, switchAddr, instructions, addrToIndex)
            val sorted = newElements.sortedBy { it.key }
            // packed-switch keys must be consecutive ASC; if rewrite broke that, the only
            // way forward would be to widen to sparse-switch, which resizes the payload and
            // shifts every subsequent offset. Out of scope here — flag and pass through.
            if (!isConsecutive(sorted)) {
                collector.warn(
                    "packed-switch hash-switch keys are no longer consecutive after string rewrite at " +
                        "switch addr=0x${switchAddr.toString(16)}; payload left unchanged"
                )
                return payload
            }
            return ImmutablePackedSwitchPayload(sorted)
        }

        /**
         * Only the first `const-string` in each case target is rehashed. If the original
         * switch carried hash-collided cases (two strings sharing one key, dispatched via
         * a chain of `const-string + equals` checks inside one block), the second branch
         * may stop matching after rewrite. Out of scope for this pass.
         */
        private fun recomputedElements(
            elements: List<SwitchElement>,
            switchAddr: Int,
            instructions: List<Instruction>,
            addrToIndex: Map<Int, Int>,
        ): List<SwitchElement> = elements.map { element ->
            val targetIdx = addrToIndex[switchAddr + element.offset]
            val caseString = targetIdx?.let { firstCaseString(instructions, it) }
            if (caseString == null) element else ImmutableSwitchElement(caseString.hashCode(), element.offset)
        }

        /** Accepts both `const-string` (16-bit string idx) and `const-string/jumbo` (32-bit). */
        private fun firstCaseString(instructions: List<Instruction>, targetIdx: Int): String? {
            val insn = instructions.getOrNull(targetIdx) ?: return null
            if (insn.opcode != Opcode.CONST_STRING && insn.opcode != Opcode.CONST_STRING_JUMBO) return null
            val ref = (insn as? ReferenceInstruction)?.reference as? StringReference ?: return null
            return ref.string
        }

        private fun isConsecutive(sortedElements: List<SwitchElement>): Boolean {
            for (i in 1 until sortedElements.size) {
                if (sortedElements[i].key - sortedElements[i - 1].key != 1) return false
            }
            return true
        }
    }
}
