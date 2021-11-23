import com.avito.android.module_type.ModuleType

public abstract class StubModuleType : ModuleType {

    override fun isEqualTo(other: ModuleType): Boolean {
        return this.javaClass == other.javaClass
    }

    override fun description(): String {
        return this::class.java.simpleName
    }
}

// Flat module types for simplicity of test fixtures
// In real applications they can be more complex

public object AppModule : StubModuleType()

public object FeatureModule : StubModuleType()

public object LibraryModule : StubModuleType()

public object TestModule : StubModuleType()
