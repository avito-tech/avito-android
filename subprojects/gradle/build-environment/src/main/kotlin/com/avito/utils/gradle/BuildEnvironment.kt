package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

/**
 * @param reason Причина почему было выбрано то или иное окружение
 */
sealed class BuildEnvironment(
    private val providers: ProviderFactory,
    private val reason: String,
    val inGradleTestKit: Boolean = isRunInGradleTestKit(providers)
) {
    class Local(providers: ProviderFactory, reason: String) : BuildEnvironment(providers, reason)
    class GradleTestKit(providers: ProviderFactory, reason: String) : BuildEnvironment(providers, reason)
    class Mirkale(providers: ProviderFactory, reason: String) : BuildEnvironment(providers, reason)
    class IDE(providers: ProviderFactory, reason: String) : BuildEnvironment(providers, reason)
    class CI(providers: ProviderFactory, reason: String) : BuildEnvironment(providers, reason)

    override fun toString(): String = "${javaClass.simpleName}, because: $reason"
}

// используется в groovy скриптах
fun buildEnvironment(project: Project): BuildEnvironment = project.buildEnvironment

val Project.buildEnvironment by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    val result = when {
        isRunInCI(project) -> BuildEnvironment.CI(
            project.providers,
            "has 'ci' gradle property with true value"
        )
        isRunViaMirakle(project) -> BuildEnvironment.Mirkale(
            project.providers,
            "has 'mirakle.build.on.remote' property"
        )
        isRunFromIDE(project) -> BuildEnvironment.IDE(
            project.providers,
            "has 'android.injected.invoked.from.ide' start parameter"
        )
        isRunFromGradleTestKit(project) -> BuildEnvironment.GradleTestKit(
            project.providers,
            "has injected.from.gradle_testkit start parameter"
        )
        isKotlinDSLResolving(project) -> BuildEnvironment.IDE(
            project.providers,
            "has 'org.gradle.kotlin.dsl.provider.script' start parameter"
        )
        else -> BuildEnvironment.Local(
            project.providers,
            "nothing else matched"
        )
    }
    result
}

fun isRunFromGradleTestKit(project: Project): Boolean {
    return project.hasProperty("injected.from.gradle_testkit")
}

@Suppress("UnstableApiUsage")
fun isRunInGradleTestKit(providers: ProviderFactory): Boolean =
    providers.systemProperty("isTest")
        .forUseAtConfigurationTime()
        .getOrElse("false")
        .toBoolean()

private fun isRunViaMirakle(project: Project): Boolean {
    return project.hasProperty("mirakle.build.on.remote")
}

private fun isRunInCI(project: Project): Boolean =
    project.hasProperty("ci") && project.property("ci") == "true"

private fun isKotlinDSLResolving(project: Project): Boolean {
    val properties = project.gradle.startParameter.projectProperties
    val scriptKey = "org.gradle.kotlin.dsl.provider.script"
    return properties.containsKey(scriptKey) && !properties[scriptKey].isNullOrBlank()
}

private fun isRunFromIDE(project: Project): Boolean {
    val properties = project.gradle.startParameter.projectProperties
    val ideRunKey = "android.injected.invoked.from.ide"
    return properties.containsKey(ideRunKey) && properties[ideRunKey] == "true"
}
