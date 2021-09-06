package com.avito.kotlin.dsl

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import java.io.File
import kotlin.reflect.KProperty

// TODO these methods should return Property<X>

/**
 * @param nullIfBlank we accept cases when user passes empty string to override
 * todo true by default, false to not break anything that rely on previous behavior
 */
@JvmOverloads
public fun Project.getOptionalStringProperty(name: String, nullIfBlank: Boolean = false): String? =
    if (hasProperty(name)) {
        val string = property(name)?.toString()
        if (nullIfBlank && string.isNullOrBlank()) null else string
    } else {
        null
    }

@JvmOverloads
public fun Project.getOptionalStringProperty(name: String, default: String, defaultIfBlank: Boolean = true): String =
    getOptionalStringProperty(name, nullIfBlank = defaultIfBlank) ?: default

/**
 * @param allowBlank todo false by default
 */
@JvmOverloads
public fun Project.getMandatoryStringProperty(name: String, allowBlank: Boolean = true): String {
    return if (hasProperty(name)) {
        val string = property(name)?.toString()
        if (string.isNullOrBlank()) {
            if (allowBlank) {
                ""
            } else {
                throw RuntimeException("Parameter: $name is blank but required")
            }
        } else {
            string
        }
    } else {
        throw RuntimeException("Parameter: $name is missing but required")
    }
}

public fun Project.getOptionalIntProperty(name: String): Int? =
    try {
        getOptionalStringProperty(name, nullIfBlank = true)?.toInt()
    } catch (e: NumberFormatException) {
        null
    }

public fun Project.getOptionalIntProperty(name: String, default: Int): Int =
    try {
        getOptionalStringProperty(name, nullIfBlank = true)?.toInt() ?: default
    } catch (e: NumberFormatException) {
        default
    }

public fun Project.getMandatoryIntProperty(name: String): Int =
    getOptionalIntProperty(name) ?: throw RuntimeException("Parameter: $name is required (must be digit)")

@JvmOverloads
public fun Project.getBooleanProperty(name: String, default: Boolean = false): Boolean =
    getOptionalStringProperty(name, nullIfBlank = true)?.toBoolean() ?: default

@JvmOverloads
public fun Project.getOptionalFloatProperty(name: String, default: Float? = null): Float? =
    try {
        getOptionalStringProperty(name, nullIfBlank = true)?.toFloat() ?: default
    } catch (e: NumberFormatException) {
        default
    }

public fun Project.fileProperty(file: File): RegularFileProperty = objects.fileProperty().apply { set { file } }

public fun Project.isRoot(): Boolean = project == project.rootProject

public object ProjectProperty {

    /**
     * You can write a value only one time.
     * Before that, it's gonna return a fallback if any.
     */
    public fun <T : Any> lateinit(
        key: String? = null,
        fallbackValue: T? = null
    ): PropertyInProjectExtras<T> = PropertyInProjectExtras(key, fallbackValue)

    // TODO: return lazy Provider<T>
    public fun <T : Any> lazy(
        key: String? = null,
        scope: PropertyScope = PropertyScope.PER_PROJECT,
        factory: (project: Project) -> T
    ): LazyPropertyInProjectExtras<T> = LazyPropertyInProjectExtras(key, scope, factory)
}

/**
 * Use [ProjectProperty.lazy] to gain instance
 */
public class PropertyInProjectExtras<T : Any>(
    private val key: String?,
    private val fallbackValue: T?
) {

    public operator fun getValue(thisRef: Project, property: KProperty<*>): T {
        val key = key ?: property.name
        if (!thisRef.extensions.extraProperties.has(key) && fallbackValue != null) {
            return fallbackValue
        }
        @Suppress("UNCHECKED_CAST")
        return thisRef.extensions.extraProperties[key] as T
    }

    public operator fun setValue(thisRef: Project, property: KProperty<*>, value: T) {
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
public class LazyPropertyInProjectExtras<T : Any>(
    private val key: String?,
    private val scope: PropertyScope,
    private val factory: (project: Project) -> T
) {

    public operator fun getValue(thisRef: Project, property: KProperty<*>): T {
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
public fun <T : Any> Project.lazyProperty(
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

public enum class PropertyScope { PER_PROJECT, ROOT_PROJECT }
