import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType

public abstract class StubModuleType(type: FunctionalType) : ModuleType(StubApplication, type) {

    override fun isEqualTo(other: ModuleType): Boolean {
        return this.javaClass == other.javaClass
    }

    override fun description(): String {
        return this::class.java.simpleName
    }
}

@Suppress("DEPRECATION")
public data object FeatureModule : StubModuleType(FunctionalType.Feature)

@Suppress("DEPRECATION")
public data object LibraryModule : StubModuleType(FunctionalType.Library)

public data object TestModule : StubModuleType(FunctionalType.Test)

public data object CommonApp : ApplicationDeclaration {
    override val name: String
        get() = "Common"
}

public data object StubApplication : ApplicationDeclaration {
    override val name: String
        get() = "stub"
}
