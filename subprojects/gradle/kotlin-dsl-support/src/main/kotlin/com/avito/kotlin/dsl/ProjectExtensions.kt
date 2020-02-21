package com.avito.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import java.io.File
import kotlin.reflect.KProperty

fun Project.getOptionalStringProperty(name: String): String? {
    return if (hasProperty(name)) {
        property(name)?.toString()
    } else {
        null
    }
}

fun Project.getOptionalStringProperty(name: String, default: String): String {
    return if (hasProperty(name)) {
        property(name)?.toString() ?: default
    } else {
        default
    }
}

fun Project.getMandatoryStringProperty(name: String): String =
    getOptionalStringProperty(name) ?: throw RuntimeException("Parameter: $name is required (must be not empty)")

fun Project.getOptionalIntProperty(name: String): Int? = try {
    getOptionalStringProperty(name)?.toInt()
} catch (e: NumberFormatException) {
    null
}

fun Project.getOptionalIntProperty(name: String, default: Int): Int = try {
    getOptionalStringProperty(name)?.toInt() ?: default
} catch (e: NumberFormatException) {
    default
}

fun Project.getMandatoryIntProperty(name: String): Int =
    getOptionalIntProperty(name) ?: throw RuntimeException("Parameter: $name is required (must be digit)")

@JvmOverloads
fun Project.getBooleanProperty(name: String, default: Boolean = false): Boolean =
    getOptionalStringProperty(name)?.toBoolean() ?: default

@JvmOverloads
fun Project.getOptionalFloatProperty(name: String, default: Float? = null): Float? {
    return if (hasProperty(name)) {
        try {
            property(name)?.toString()?.toFloat() ?: default
        } catch (e: NumberFormatException) {
            default
        }
    } else {
        default
    }
}

@Suppress("UnstableApiUsage")
fun Project.fileProperty(file: File): RegularFileProperty = objects.fileProperty().apply { set { file } }

fun Project.isRoot() = (project == project.rootProject)

object ProjectProperty {

    /**
     * You can write a value only one time.
     * Before that, it's gonna return a fallback if any.
     */
    fun <T : Any> lateinit(
        key: String? = null,
        fallbackValue: T? = null
    ) = PropertyInProjectExtras(key, fallbackValue)

    // TODO: return lazy Provider<T>
    fun <T : Any> lazy(
        key: String? = null,
        scope: PropertyScope = PropertyScope.PER_PROJECT,
        factory: (project: Project) -> T
    ) = LazyPropertyInProjectExtras(key, scope, factory)
}

/**
 * Use [ProjectProperty.lazy] to gain instance
 */
class PropertyInProjectExtras<T : Any>(
    private val key: String?,
    private val fallbackValue: T?
) {

    operator fun getValue(thisRef: Project, property: KProperty<*>): T {
        val key = key ?: property.name
        if (!thisRef.extensions.extraProperties.has(key) && fallbackValue != null) {
            return fallbackValue
        }
        @Suppress("UNCHECKED_CAST")
        return thisRef.extensions.extraProperties[key] as T
    }

    operator fun setValue(thisRef: Project, property: KProperty<*>, value: T) {
        val key = key ?: property.name
        if (thisRef.extensions.extraProperties.has(key)) {
            error("$key is already set for ${thisRef.path}.ext; This is not normal")
        }
        thisRef.extensions.extraProperties.set(key, value)
    }
}

/**
 * Use [ProjectProperty.lazy] to gain instance
 */
class LazyPropertyInProjectExtras<T : Any>(
    private val key: String?,
    private val scope: PropertyScope,
    private val factory: (project: Project) -> T
) {

    operator fun getValue(thisRef: Project, property: KProperty<*>): T {
        val key = key ?: property.name + "_cached_prop"
        val project = when (scope) {
            PropertyScope.PER_PROJECT -> thisRef
            PropertyScope.ROOT_PROJECT -> thisRef.rootProject
        }
        ensureInitialized(project, key)

        @Suppress("UNCHECKED_CAST")
        return project.extensions.extraProperties[key] as T
    }

    @Synchronized
    private fun ensureInitialized(project: Project, key: String) {
        if (!project.extensions.extraProperties.has(key)) {
            project.extensions.extraProperties[key] = factory(project)
        }
    }
}

// TODO: merge with ProjectProperty
fun <T : Any> Project.lazyProperty(
    name: String,
    factory: (project: Project) -> T
): T {
    val root = this.rootProject
    return if (root.extensions.extraProperties.has(name)) {
        @Suppress("UNCHECKED_CAST")
        root.extensions.extraProperties[name] as T
    } else {
        val value = factory(this)
        root.extensions.extraProperties[name] = value
        value
    }
}

enum class PropertyScope { PER_PROJECT, ROOT_PROJECT }
