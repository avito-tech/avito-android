package com.avito.android.di.spi

import dagger.spi.model.Binding
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingGraphPlugin
import dagger.spi.model.ComponentPath
import dagger.spi.model.DiagnosticReporter
import javax.tools.Diagnostic

public class DisallowFakeStubDependenciesPlugin : BindingGraphPlugin {

    override fun pluginName(): String = "DisallowFakeStubDependencies"

    override fun visitGraph(bindingGraph: BindingGraph, diagnosticReporter: DiagnosticReporter) {
        val bindingsByComponent = bindingGraph.bindings().groupBy { it.componentPath() }

        bindingGraph.componentNodes().forEach {
            checkComponentNode(it, bindingsByComponent, diagnosticReporter)
        }
    }

    private fun checkComponentNode(
        componentNode: BindingGraph.ComponentNode,
        bindingsByComponent: Map<ComponentPath, List<Binding>>,
        diagnosticReporter: DiagnosticReporter
    ) {
        val componentPath = componentNode.componentPath()
        val componentSimpleName = componentPath.currentComponent().ksp().simpleName.asString()

        if (componentSimpleName.containsDemoOrTest() || componentSimpleName.containsFakeOrStub()) {
            return
        }

        val bindingsForComponent = bindingsByComponent[componentPath] ?: return

        for (binding in bindingsForComponent) {
            checkBinding(binding, diagnosticReporter, componentSimpleName)
        }
    }

    private fun checkBinding(
        binding: Binding,
        diagnosticReporter: DiagnosticReporter,
        componentSimpleName: String
    ) {
        val declaration = binding.key().type().ksp().declaration
        val simpleName = declaration.simpleName.asString()

        if (!simpleName.containsFakeOrStub()) {
            return
        }

        if (declaration.annotations.any { it.shortName.getShortName() == "AllowedInProduction" }) {
            return
        }

        diagnosticReporter.reportBinding(
            Diagnostic.Kind.ERROR,
            binding,
            "Component %s has forbidden provided type %s which contains Fake/Stub in its name. " +
                "See docs for details: https://links.k.avito.ru/android-disallow-fake-stub-dependencies",
            componentSimpleName,
            simpleName
        )
    }

    private fun String.containsFakeOrStub() = contains("Fake") || contains("Stub")
    private fun String.containsDemoOrTest() = contains("Demo") || contains("Test")
}
