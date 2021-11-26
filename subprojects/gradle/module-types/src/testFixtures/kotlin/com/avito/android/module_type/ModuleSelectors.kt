import com.avito.android.module_type.DependencyMatcher
import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType

public class BetweenModuleTypes(
    private val module: StubModuleType,
    private val dependency: StubModuleType,
    private val configuration: ConfigurationType = ConfigurationType.Main
) : DependencyMatcher {

    override fun matches(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        val moduleType = module.type ?: return false
        val dependencyType = dependency.type ?: return false

        return this.module.isEqualTo(moduleType)
            && this.dependency.isEqualTo(dependencyType)
            && this.configuration == configuration
    }

    override fun description(): String =
        "module of type ${module.description()} depends on a module of type ${dependency.description()}"
}

public class ToTestModule : DependencyMatcher {

    override fun matches(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        val moduleType = module.type ?: return false
        val dependencyType = dependency.type ?: return false

        return !moduleType.isEqualTo(TestModule)
            && dependencyType.isEqualTo(TestModule)
            && configuration == ConfigurationType.Main
    }

    override fun description(): String =
        "module uses test dependency not in tests"
}

public class DependentModule(
    public val path: String
) : DependencyMatcher {

    override fun matches(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean =
        path == dependency.path

    override fun description(): String =
        "dependent module $path"
}
