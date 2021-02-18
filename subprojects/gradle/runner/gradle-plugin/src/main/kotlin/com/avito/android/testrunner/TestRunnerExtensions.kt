package com.avito.android.testrunner

import com.avito.utils.gradle.KubernetesCredentials
import org.gradle.api.provider.Property

public abstract class TestRunnerExtensions {

    public abstract val kubernetesCredentials: Property<KubernetesCredentials>
}
