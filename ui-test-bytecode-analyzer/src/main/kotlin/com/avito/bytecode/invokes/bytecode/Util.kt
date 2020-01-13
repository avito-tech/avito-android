package com.avito.bytecode.invokes.bytecode

import org.apache.bcel.classfile.Method
import org.apache.bcel.generic.ConstantPoolGen
import org.apache.bcel.generic.InvokeInstruction

fun InvokeInstruction.getFullMethodName(pool: ConstantPoolGen) =
    "${getMethodName(pool)}(${getArgumentTypes(pool).joinToString(" ") { it.toString() }})"

inline val Method.fullName: String
    get() = "$name(${argumentTypes.joinToString(" ") { it.toString() }})"