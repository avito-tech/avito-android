import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UncheckedIOException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.HelpTasksPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.util.PropertiesUtils
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.files
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.IOException
import java.util.Properties

class TestKitHelperPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val testKitConfig = target.configurations.register(ADDITIONAL_TEST_KIT_CONFIGURATION)

        val javaConvention = target.convention.getPlugin<JavaPluginConvention>()

        val defaultTestSourceSet: SourceSet = javaConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)

        val testKitExtension =
            target.extensions.create<TestKitHelperExtension>(
                TEST_KIT_EXTENSION_NAME,
                setOf(defaultTestSourceSet)
            )

        val testKitClasspathTask = target.tasks.register<TestClassPathTask>("additionalTestKitClasspath") {
            group = HelpTasksPlugin.HELP_GROUP

            pluginClasspath.from(testKitConfig.get().files)
            outputDirectory.set(project.layout.buildDirectory.dir(name))
        }

        target.afterEvaluate {
            testKitExtension.testSourceSets.forEach { testSourceSet ->
                val runtimeOnlyConfigurationName = testSourceSet.runtimeOnlyConfigurationName
                dependencies.add(runtimeOnlyConfigurationName, target.layout.files(testKitClasspathTask))
            }
        }
    }
}

open class TestKitHelperExtension(var testSourceSets: Set<SourceSet> = emptySet())

abstract class TestClassPathTask : DefaultTask() {

    @Input
    protected open fun getPaths(): List<String> =
        pluginClasspath.map { file -> file.absolutePath.replace("\\\\".toRegex(), "/") }

    @get:Classpath
    abstract val pluginClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun doWork() {
        val properties = Properties().apply {
            setProperty(ADDITIONAL_TEST_KIT_CLASSPATH_KEY, implementationClasspath())
        }
        val propertiesFile = File(outputDirectory.get().asFile, ADDITIONAL_TEST_KIT_PROPERTIES_FILE)
        saveProperties(properties, propertiesFile)
    }

    private fun implementationClasspath(): String? {
        return getPaths().joinToString(separator = File.pathSeparator)
    }

    private fun saveProperties(properties: Properties, outputFile: File) {
        try {
            PropertiesUtils.store(properties, outputFile)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}

const val ADDITIONAL_TEST_KIT_CONFIGURATION = "testKit"
const val TEST_KIT_EXTENSION_NAME = "testKitHelper"
const val ADDITIONAL_TEST_KIT_CLASSPATH_KEY = "additional-testkit-classpath"
const val ADDITIONAL_TEST_KIT_PROPERTIES_FILE = "additional-testkit-classpath.properties"
