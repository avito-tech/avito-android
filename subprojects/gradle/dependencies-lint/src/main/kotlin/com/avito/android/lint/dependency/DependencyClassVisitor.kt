/*
 * Copyright 2015-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright (C) 2020 Avito
 */

package com.avito.android.lint.dependency

import com.avito.android.lint.util.typeHierarchy
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.TypePath
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor
import org.slf4j.LoggerFactory

internal class ArtifactUsage(
    val artifact: ResolvedArtifactResult,
    /**
     * Using class which was analyzed -> Used class from dependency
     */
    val classesUsages: MutableSet<Pair<String, String>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArtifactUsage) return false

        if (artifact != other.artifact) return false

        return true
    }

    override fun hashCode(): Int {
        return artifact.hashCode()
    }
}


internal class DependencyClassVisitor(
    private val classOwners: Map<String, Set<ResolvedArtifactResult>>,
    private val loader: ClassLoader
) : ClassVisitor(Opcodes.ASM7) {

    private lateinit var className: String
    private val logger = LoggerFactory.getLogger(DependencyClassVisitor::class.java)

    val directReferences: MutableSet<ArtifactUsage> = mutableSetOf()

    /**
     * References that are necessary at compile time (e.g. type hierarchy of implemented interfaces), but are satisfactory
     * as transitive dependencies.
     * Example: guice's <code>GuiceServletContextListener</code> refers to javax.servlet's
     * <code>ServletContextListener</code>, but adding guice as a dependency does NOT result in a transitive dependency on
     * javax.servlet. In this case, we want to preserve a first order dependency on javax.servlet when
     * <code>GuiceServletContextListener</code> is extended somewhere in the code.
     */
    val indirectReferences: MutableSet<ArtifactUsage> = mutableSetOf()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        className = name
        readObjectName(superName)
        interfaces?.forEach { readObjectName(it) }

        if (superName != null) {
            try {
                val clazz = Class.forName(superName.replace('/', '.'), false, loader)
                typeHierarchy(clazz).forEach {
                    readObjectName(it.replace('.', '/'), indirect = true)
                }
            } catch (error: ClassNotFoundException) {
                logger.debug("Can't load class $superName")
            } catch (error: NoClassDefFoundError) {
                logger.debug("Can't load class $superName")
            }
        }
        interfaces?.forEach { intf ->
            try {
                val clazz = Class.forName(intf.replace('/', '.'), false, loader)
                typeHierarchy(clazz).forEach {
                    readObjectName(it.replace('.', '/'), indirect = true)
                }
            } catch (error: ClassNotFoundException) {
                logger.debug("Can't load class $intf")
            }
        }
        readSignature(signature)
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
        readType(descriptor)
        return DependencyAnnotationVisitor()
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        readType(descriptor)
        return DependencyAnnotationVisitor() // TODO: check kotlin metadata for lost ABI
    }

    override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor {
        readType(descriptor)
        readSignature(signature)
        return DependencyFieldVisitor()
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        Type.getArgumentTypes(descriptor).forEach { readType(it.descriptor) }
        readType(Type.getReturnType(descriptor).descriptor)
        readSignature(signature)
        exceptions?.forEach { readObjectName(it) }
        return DependencyMethodVisitor()
    }

    private fun readObjectName(type: String?, indirect: Boolean = false) {
        if (type == null) return
        val targetClassName = Type.getObjectType(type).internalName
        val owners = classOwners[targetClassName] ?: return
        if (logger.isDebugEnabled) {
            for (owner in owners) {
                logger.debug("$className refers to $type which was found in $owner")
            }
        }
        if (indirect) addArtifactUsage(indirectReferences, targetClassName, owners)
        else addArtifactUsage(directReferences, targetClassName, owners)
    }

    private fun addArtifactUsage(usages: MutableSet<ArtifactUsage>, targetClassName: String, artifacts: Set<ResolvedArtifactResult>) {
        artifacts.forEach { artifact ->
            val usage = usages.find { it.artifact == artifact }
            if (usage == null) {
                usages.add(ArtifactUsage(artifact, mutableSetOf(className to targetClassName)))
            } else {
                usage.classesUsages.add(className to targetClassName)
            }
        }
    }

    private fun readType(desc: String?) {
        if (desc == null) return
        val t = Type.getType(desc)
        when (t.sort) {
            Type.ARRAY -> readType(t.elementType.descriptor)
            Type.OBJECT -> readObjectName(t.internalName)
            else -> readObjectName(desc)
        }
    }

    private fun readSignature(signature: String?) {
        if (signature != null) {
            SignatureReader(signature).accept(DependencySignatureVisitor())
        }
    }

    inner class DependencySignatureVisitor : SignatureVisitor(Opcodes.ASM7) {
        override fun visitClassType(name: String?) {
            readObjectName(name)
        }
    }

    inner class DependencyFieldVisitor : FieldVisitor(Opcodes.ASM7) {
        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }
    }

    inner class DependencyAnnotationVisitor : AnnotationVisitor(Opcodes.ASM7) {
        override fun visit(name: String?, value: Any?) {
            if (value is Type) {
                readObjectName(value.internalName)
            }
        }

        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            readType(descriptor)
        }
    }

    inner class DependencyMethodVisitor : MethodVisitor(Opcodes.ASM7) {

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
            readObjectName(owner)
            readType(Type.getReturnType(descriptor).descriptor)
            Type.getArgumentTypes(descriptor).forEach { readType(it.descriptor) }
        }

        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
            readType(descriptor)
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            readObjectName(type)
        }

        override fun visitInvokeDynamicInsn(name: String?, descriptor: String?, bootstrapMethodHandle: Handle?, vararg bootstrapMethodArguments: Any?) {
            readType(descriptor)
        }

        override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }

        override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
            readType(descriptor)
            return super.visitAnnotation(descriptor, visible)
        }

        override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
            readObjectName(type)
        }

        override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }

        override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
            readType(descriptor)
            readSignature(signature)
        }

        override fun visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath?, start: Array<out Label>?, end: Array<out Label>?, index: IntArray?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }

        override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
            readType(descriptor)
            return DependencyAnnotationVisitor()
        }

        override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
            readType(descriptor)
        }

        override fun visitLdcInsn(value: Any?) {
            if (value is Type) {
                readObjectName(value.internalName)
            }
        }
    }
}
