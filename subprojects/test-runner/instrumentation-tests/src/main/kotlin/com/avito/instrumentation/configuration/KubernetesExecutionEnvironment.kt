package com.avito.instrumentation.configuration

import com.avito.k8s.model.toleration.TolerationConfig
import com.avito.k8s.model.toleration.TolerationEffect
import com.avito.k8s.model.toleration.TolerationOperator
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

public interface KubernetesExecutionEnvironment : ExecutionEnvironment {

    public val namespace: Property<String>

    public val tolerations: ListProperty<TolerationConfig>

    public fun tolerations(action: TolerationsConfigurationAction.() -> Unit) {
        val configurator = TolerationsConfigurationAction()
        action(configurator)
        tolerations.set(configurator.tolerations)
    }

    public class TolerationsConfigurationAction {
        internal val tolerations = mutableListOf<TolerationConfig>()

        public fun toleration(
            key: String,
            operator: TolerationOperator,
            value: String,
            effect: TolerationEffect,
        ) {
            tolerations.add(
                TolerationConfig(
                    key = key,
                    operator = operator,
                    value = value,
                    effect = effect
                )
            )
        }
    }
}
