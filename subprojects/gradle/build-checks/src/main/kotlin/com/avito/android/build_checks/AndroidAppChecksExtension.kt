package com.avito.android.build_checks

import com.android.resources.ResourceType
import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck.UniqueAppResources
import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck.UniqueRClasses
import com.avito.android.build_checks.internal.unique_app_res.Resource
import org.gradle.api.Action
import kotlin.reflect.full.createInstance

public open class AndroidAppChecksExtension : BuildChecksExtension() {

    override val allChecks: List<Check>
        get() {
            return AndroidAppCheck::class.sealedSubclasses
                .map { it.createInstance() }
        }

    public fun uniqueRClasses(action: Action<UniqueRClasses>): Unit =
        register(UniqueRClasses(), action)

    public fun uniqueAppResources(action: Action<UniqueAppResources>): Unit =
        register(UniqueAppResources(), action)

    public sealed class AndroidAppCheck : Check {

        public override var enabled: Boolean = true

        public open class UniqueRClasses : AndroidAppCheck() {
            public val allowedNonUniquePackageNames: MutableList<String> = mutableListOf()
        }

        public open class UniqueAppResources : Check, RequireValidation {
            // Disabled by default due to probable false-positive errors
            override var enabled: Boolean = false

            /**
             * Types to ignore: layout, string, integer, ... as it used in XML.
             * See all names in [com.android.resources.ResourceType]
             */
            public val ignoredResourceTypes: MutableList<String> = mutableListOf()

            internal val ignoredResources: MutableSet<Resource> = mutableSetOf()

            /**
             * Key is a resource type: layout, string, integer, ... as it used in XML.
             * See all names in [com.android.resources.ResourceType]
             *
             * Value: resource name
             */
            public fun ignoredResource(type: String, name: String) {
                require(name.isNotBlank()) {
                    "Resource can't have empty name (type: $type)"
                }
                val resourceType = parseResourceType(type)
                ignoredResources.add(Resource(resourceType, name))
            }

            override fun validate() {
                ignoredResourceTypes.forEach {
                    parseResourceType(it)
                }
            }

            private fun parseResourceType(type: String): ResourceType {
                return try {
                    requireNotNull(ResourceType.fromClassName(type))
                } catch (e: Exception) {
                    throw IllegalArgumentException(
                        "Unknown resource type '$type'. " +
                            "See available values in com.android.resources.ResourceType's name.",
                        e
                    )
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            return this.javaClass == other?.javaClass
        }
    }
}
