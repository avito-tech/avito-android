package com.avito.bytecode

/**
 * Use equals and hashcode just for nameWithArguments field because metadata (like
 * isScreenGetter or isTest) can be not equals on right and on left side
 * in invocation operation.
 */
class Node(
    val className: String,
    val methodName: String,
    val methodNameWithArguments: String,
    val isTest: Boolean?,
    val name: String = "$className.$methodName",
    val nameWithArguments: String = "$className.$methodNameWithArguments"
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (nameWithArguments != other.nameWithArguments) return false

        return true
    }

    override fun hashCode(): Int {
        return nameWithArguments.hashCode()
    }

    override fun toString(): String {
        return "Node(nameWithArguments='$nameWithArguments', isTest=$isTest)"
    }
}
