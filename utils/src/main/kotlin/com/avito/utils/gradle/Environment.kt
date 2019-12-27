package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import org.gradle.api.Project
import java.net.InetAddress

/**
 * @param reason Причина почему было выбрано то или иное окружение
 */
sealed class BuildEnvironment(
    private val reason: String,
    val inGradleTestKit: Boolean = isRunInGradleTestKit()
) {
    class Local(reason: String) : BuildEnvironment(reason)
    class GradleTestKit(reason: String) : BuildEnvironment(reason)
    class Mainframer(reason: String) : BuildEnvironment(reason)
    class IDE(reason: String) : BuildEnvironment(reason)
    class CI(reason: String) : BuildEnvironment(reason)

    override fun toString(): String = "${javaClass.simpleName}, because: $reason"
}

// используется в groovy скриптах
fun buildEnvironment(project: Project): BuildEnvironment = project.buildEnvironment

val Project.buildEnvironment by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    when {
        isRunInCI(project) -> BuildEnvironment.CI(
            "has 'ci' gradle property with true value"
        )
        isRunInMainframer() -> BuildEnvironment.Mainframer(
            "hostName is android-builder.msk.avito.ru or android-builder"
        )
        isRunFromIDE(project) -> BuildEnvironment.IDE(
            "has 'android.injected.invoked.from.ide' start parameter"
        )
        isRunFromGradleTestKit(project) -> BuildEnvironment.GradleTestKit(
            "has injected.from.gradle_testkit start parameter"
        )
        isKotlinDSLResolving(project) -> BuildEnvironment.IDE(
            "has 'org.gradle.kotlin.dsl.provider.script' start parameter"
        )
        else -> BuildEnvironment.Local(
            "nothing else matched"
        )
    }
}

fun isRunFromGradleTestKit(project: Project): Boolean {
    return project.hasProperty("injected.from.gradle_testkit")
}

fun isRunInGradleTestKit(): Boolean =
    System.getProperty("isTest", "false") == "true"

private fun isRunInMainframer(): Boolean {
    val hostName = InetAddress.getLocalHost().hostName

    return hostName == "android-builder.msk.avito.ru" || hostName == "android-builder"
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
