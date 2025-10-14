package com.avito.android.contracts.platform.extension.configurations.validation

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public abstract class ValidationConfiguration @Inject constructor(objects: ObjectFactory) : Named {

    public val rulesGroups: NamedDomainObjectContainer<ValidationRulesGroup> = objects.domainObjectContainer(
        ValidationRulesGroup::class.java
    )
}
