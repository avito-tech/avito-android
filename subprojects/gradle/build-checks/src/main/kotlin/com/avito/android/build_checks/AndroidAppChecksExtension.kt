package com.avito.android.build_checks

import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck.UniqueRClasses
import org.gradle.api.Action
import kotlin.reflect.full.createInstance

public open class AndroidAppChecksExtension : BuildChecksExtension() {

    public fun uniqueRClasses(action: Action<UniqueRClasses>): Unit =
        register(UniqueRClasses(), action)

    override val allChecks: List<Check>
        get() {
            return AndroidAppCheck::class.sealedSubclasses
                .map { it.createInstance() }
        }

    public sealed class AndroidAppCheck : Check {

        public override var enabled: Boolean = true

        public open class UniqueRClasses : AndroidAppCheck() {
            public val allowedNonUniquePackageNames: MutableList<String> = mutableListOf()
        }

        override fun equals(other: Any?): Boolean {
            return this.javaClass == other?.javaClass
        }
    }
}
