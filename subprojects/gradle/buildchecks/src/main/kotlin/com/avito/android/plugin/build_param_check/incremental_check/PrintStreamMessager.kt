package com.avito.android.plugin.build_param_check.incremental_check

import java.io.PrintStream
import javax.annotation.processing.Messager
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.tools.Diagnostic

internal class PrintStreamMessager(
    private val writer: PrintStream = System.out,
    private val tag: String = ""
) : Messager {

    override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?) {
        val prefix = if (tag.isEmpty()) "" else "$tag: "
        writer.println("$prefix($kind) $msg")
    }

    override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?, e: Element?) {
        printMessage(kind, msg)
    }

    override fun printMessage(kind: Diagnostic.Kind?, msg: CharSequence?, e: Element?, am: AnnotationMirror?) {
        printMessage(kind, msg)
    }

    override fun printMessage(
        kind: Diagnostic.Kind?,
        msg: CharSequence?,
        element: Element?,
        mirror: AnnotationMirror?,
        value: AnnotationValue?
    ) {
        printMessage(kind, msg)
    }

}
