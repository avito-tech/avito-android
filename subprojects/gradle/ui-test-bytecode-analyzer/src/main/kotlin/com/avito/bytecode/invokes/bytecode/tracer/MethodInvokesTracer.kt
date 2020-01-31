package com.avito.bytecode.invokes.bytecode.tracer

import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.invokes.model.InvocationFrom
import com.avito.bytecode.invokes.model.InvocationTo
import com.avito.bytecode.invokes.model.Invoke
import com.avito.bytecode.invokes.test.isBefore
import com.avito.bytecode.invokes.test.isTest
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method
import org.apache.bcel.generic.ConstantPoolGen
import org.apache.bcel.generic.EmptyVisitor
import org.apache.bcel.generic.Instruction
import org.apache.bcel.generic.InstructionHandle
import org.apache.bcel.generic.InvokeInstruction
import org.apache.bcel.generic.MethodGen

interface MethodInvokesTracer {
    /**
     * Non thread-safe
     */
    fun trace(
        clazz: JavaClass,
        method: Method,
        listener: InvokesListener
    )
}

class TestAwareMethodInvokesTracer(
    private val context: Context
) : MethodInvokesTracer, EmptyVisitor() {

    private var currentRealMethodsForVisit: List<Method>? = null
    private var currentMethodGen: MethodGen? = null
    private var currentMethod: Method? = null
    private var currentClass: JavaClass? = null
    private var currentPool: ConstantPoolGen? = null
    private var currentListener: InvokesListener? = null

    override fun trace(
        clazz: JavaClass,
        method: Method,
        listener: InvokesListener
    ) {
        val methodGen = MethodGen(
            method,
            clazz.className,
            ConstantPoolGen(method.constantPool)
        )
        if (methodGen.isNative) {
            return
        }

        currentMethodGen = methodGen
        currentMethod = method
        currentClass = clazz
        currentListener = listener
        currentPool = methodGen.constantPool

        parseMethod()

        currentMethodGen = null
        currentClass = null
        currentListener = null
        currentPool = null
    }

    override fun visitInvokeInstruction(instruction: InvokeInstruction) {
        val method = currentMethod!!
        val clazz = currentClass!!
        val pool = currentPool!!
        val listener = currentListener!!

        val visitedMethods = currentRealMethodsForVisit ?: listOf(method)

        visitedMethods.forEach { fromMethod ->
            context.findAllPossibleAffectedMethodsByInvocation(instruction, pool).forEach { toMethod ->
                listener.invoke(
                    Invoke(
                        InvocationFrom(
                            method = fromMethod,
                            clazz = clazz
                        ),
                        InvocationTo(
                            className = toMethod.className,
                            methodName = toMethod.methodName,
                            methodNameWithArguments = toMethod.methodNameWithArguments
                        )
                    )
                )
            }
        }
    }

    private fun parseMethod() {
        val methodGen = currentMethodGen!!
        val clazz = currentClass!!

        var instructionHandle: InstructionHandle? = methodGen.instructionList.start

        while (instructionHandle != null) {
            val instruction = instructionHandle.instruction

            // If current visited method is marked as @Before we should apply
            // all invokes to all tests inside current class
            if (methodGen.isBefore()) {
                currentRealMethodsForVisit = context.getAllRealMethods(clazz)
                    .filter { it.isTest() }
            }

            if (shouldVisitInstruction(instruction)) {
                instruction.accept(this)
            }

            currentRealMethodsForVisit = null
            instructionHandle = instructionHandle.next
        }
    }

    private fun shouldVisitInstruction(instruction: Instruction): Boolean = instruction is InvokeInstruction
}
