package com.avito.bytecode.invokes.bytecode.context

import com.avito.bytecode.invokes.bytecode.fullName
import com.avito.bytecode.invokes.bytecode.getFullMethodName
import com.avito.bytecode.invokes.bytecode.model.FoundMethod
import org.apache.bcel.classfile.Field
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method
import org.apache.bcel.generic.ConstantPoolGen
import org.apache.bcel.generic.InvokeInstruction
import java.util.concurrent.ConcurrentHashMap

private typealias ClassName = String

class Context(
    val classes: Map<ClassName, JavaClass>
) {
    /**
     * Return all methods, that could be affected by passed invocation.
     * For example, when we have execution on abstract class or interface,
     * it means, that every implementation should be affected by this call.
     */
    fun findAllPossibleAffectedMethodsByInvocation(
        instruction: InvokeInstruction,
        pool: ConstantPoolGen
    ): Set<FoundMethod> {
        val result: MutableSet<FoundMethod> = ConcurrentHashMap.newKeySet<FoundMethod>()

        val calledClass = instruction.getReferenceType(pool).toString()
        val calledMethod = instruction.getMethodName(pool).toString()
        val calledMethodWithArguments = instruction.getFullMethodName(pool)

        findAllPossibleAffectedMethods(
            className = calledClass,
            methodName = calledMethod,
            methodNameWithArguments = calledMethodWithArguments,
            result = result
        )

        return result
    }

    private fun findAllPossibleAffectedMethods(
        className: String,
        methodName: String,
        methodNameWithArguments: String,
        result: MutableSet<FoundMethod>
    ) {
        val calledClass = classes[className]
        val calledMethod = calledClass?.let {
            getAllMethods(calledClass)
                .find { method -> method.fullName == methodNameWithArguments }
        }

        if (
        // method or class not found in sources (outside analyser's scope)
            calledClass == null ||
            // method or class not found in sources (outside analyser's scope)
            calledMethod == null ||
            // method is abstract but class isn't (it is possible when we implement it through parent implicitly)
            calledMethod.isAbstract && !calledClass.isAbstract && !calledClass.isInterface
        ) {
            result.add(
                FoundMethod(
                    className = className,
                    methodName = methodName,
                    methodNameWithArguments = methodNameWithArguments
                )
            )
            return
        }

        if (!calledMethod.isAbstract) {
            result.add(
                FoundMethod(
                    className = className,
                    methodName = methodName,
                    methodNameWithArguments = methodNameWithArguments
                )
            )

            if (calledMethod.isFinal) {
                return
            }
        }

        val dependedClasses = when {
            calledClass.isInterface -> findAllImplementationsOfClass(calledClass)
            else -> findAllInheritedClassesFromClass(calledClass)
        }

        dependedClasses.parallelStream()
            .forEach {
                findAllPossibleAffectedMethods(
                    className = it.className,
                    methodName = methodName,
                    methodNameWithArguments = methodNameWithArguments,
                    result = result
                )
            }
    }

    /**
     * Get all fields from class (and it's parents)
     */
    fun getAllFields(clazz: JavaClass): Set<Field> {
        val result: MutableSet<Field> = mutableSetOf()

        getAllFieldsRecursively(
            clazz = clazz,
            result = result
        )

        return result
    }

    private fun getAllFieldsRecursively(
        clazz: JavaClass?,
        result: MutableSet<Field>
    ) {
        if (clazz == null) {
            return
        }

        clazz.fields.forEach { result.add(it) }

        val superClass = classes[clazz.superclassName]

        getAllFieldsRecursively(
            clazz = superClass,
            result = result
        )
    }

    /**
     * Get all methods from class (and it's parents)
     */
    fun getAllMethods(clazz: JavaClass): Set<Method> {
        val result: MutableSet<Method> = mutableSetOf()

        getAllMethodsRecursively(
            clazz = clazz,
            result = result
        )

        return result
    }

    private fun getAllMethodsRecursively(
        clazz: JavaClass?,
        result: MutableSet<Method>
    ) {
        if (clazz == null) {
            return
        }

        clazz.methods.forEach { result.add(it) }

        val parents: List<JavaClass?> = clazz.interfaceNames
            .map { classes[it] }
            .plus(classes[clazz.superclassName])

        parents.forEach {
            getAllMethodsRecursively(
                clazz = it,
                result = result
            )
        }
    }

    /**
     * Get all interfaces, which implemented by passed class
     */
    fun implementationOf(clazz: JavaClass): Set<String> {
        val result: MutableSet<String> = mutableSetOf()

        implementationOfRecursively(
            clazz = clazz,
            result = result
        )

        return result
    }

    private fun implementationOfRecursively(
        clazz: JavaClass,
        result: MutableSet<String>
    ) {
        clazz.interfaceNames.forEach {
            result.add(it)

            val loadedInterface = classes[it]
            if (loadedInterface != null) {
                implementationOfRecursively(
                    clazz = loadedInterface,
                    result = result
                )
            }
        }

        val loadedSuperClass = classes[clazz.superclassName]
        if (loadedSuperClass != null) {
            implementationOfRecursively(
                clazz = loadedSuperClass,
                result = result
            )
        }
    }

    /**
     * Get all implemented (not abstract) methods from class (and it's parents)
     */
    fun getAllRealMethods(clazz: JavaClass): Set<Method> = getAllMethods(clazz)
        .asSequence()
        .filter { !it.isAbstract }
        .toSet()

    /**
     * Get all implemented (not abstract) fields from class (and it's parents)
     */
    fun getAllRealFields(clazz: JavaClass): Set<Field> = getAllFields(clazz)
        .asSequence()
        .filter { !it.isAbstract }
        .toSet()

    private fun findAllImplementationsOfClass(clazz: JavaClass): List<JavaClass> =
        classes.values.filter { it.interfaceNames.contains(clazz.className) }

    private fun findAllInheritedClassesFromClass(clazz: JavaClass): List<JavaClass> =
        classes.values.filter { it.superclassName == clazz.className }
}
