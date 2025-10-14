package com.avito.android.contracts.platform.scheme.validation.analyzer.rules

import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import javax.inject.Inject

public abstract class EmptySchemesDiagnosticRule @Inject constructor() : NetworkContractsDiagnosticRule() {

    @get:Input
    public abstract val modulePath: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val schemes: ConfigurableFileCollection

    override fun analyze() {
        if (schemes.asFileTree.files.isEmpty()) {
            report(
                NetworkContractsDiagnostic.Local(
                    issue,
                    message = "Module `${modulePath.get()}` applies plugin, " +
                        "but does not contain any network contracts schemes.",
                )
            )
        }
    }
}
