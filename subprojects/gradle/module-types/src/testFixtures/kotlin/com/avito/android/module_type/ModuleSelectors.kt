import com.avito.android.module_type.DependencyMatcher
import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType

public class BetweenModuleTypes(
    private val from: StubModuleType,
    private val to: StubModuleType,
    private val configuration: ConfigurationType = ConfigurationType.Main
) : DependencyMatcher {

    override fun match(from: ModuleWithType, to: ModuleWithType, configuration: ConfigurationType): Boolean {
        val fromType = from.type ?: return false
        val toType = to.type ?: return false

        return this.from.isEqualTo(fromType)
            && this.to.isEqualTo(toType)
            && this.configuration == configuration
    }

    override fun description(): String =
        "module of type ${from.description()} depends on a module of type ${to.description()}"
}

public class ToTestModule : DependencyMatcher {

    override fun match(from: ModuleWithType, to: ModuleWithType, configuration: ConfigurationType): Boolean {
        val fromType = from.type ?: return false
        val toType = to.type ?: return false

        return !fromType.isEqualTo(TestModule)
            && toType.isEqualTo(TestModule)
            && configuration == ConfigurationType.Main
    }

    override fun description(): String =
        "module uses test dependency not in tests"
}

public class DependentModule(
    public val path: String
) : DependencyMatcher {

    override fun match(from: ModuleWithType, to: ModuleWithType, configuration: ConfigurationType): Boolean =
        path == to.path

    override fun description(): String =
        "dependent module $path"
}
