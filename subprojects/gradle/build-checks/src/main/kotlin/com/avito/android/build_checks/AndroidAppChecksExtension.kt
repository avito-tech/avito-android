package com.avito.android.build_checks

import com.android.resources.ResourceType
import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck.UniqueAppResources
import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck.UniqueRClasses
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

            /**
             * Key is a resource type: layout, string, integer, ... as it used in XML.
             * See all names in [com.android.resources.ResourceType]
             *
             * Value: resource name
             */
            public val ignoredResources: MutableMap<String, String> = mutableMapOf()

            override fun validate() {
                ignoredResourceTypes.forEach {
                    validateResourceType(it)
                }
                ignoredResources.forEach { (type, name) ->
                    validateResourceType(type)
                    require(name.isNotBlank()) {
                        "Resource can't have empty name (type: $type)"
                    }
                }
            }

            private fun validateResourceType(type: String) {
                try {
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
