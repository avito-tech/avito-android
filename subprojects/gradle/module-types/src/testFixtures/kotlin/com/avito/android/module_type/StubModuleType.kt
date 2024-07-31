import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType

public abstract class StubModuleType(type: FunctionalType) : ModuleType(StubApplication(), type) {

    override fun isEqualTo(other: ModuleType): Boolean {
        return this.javaClass == other.javaClass
    }

    override fun description(): String {
        return this::class.java.simpleName
    }
}

@Suppress("DEPRECATION")
public object FeatureModule : StubModuleType(FunctionalType.Feature)

@Suppress("DEPRECATION")
public object LibraryModule : StubModuleType(FunctionalType.Library)

public object TestModule : StubModuleType(FunctionalType.Test)

private class StubApplication : ApplicationDeclaration {
    override val name: String
        get() = "stub"
}
