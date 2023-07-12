package com.avito.android.tech_budget

import com.avito.android.model.Owner
import com.avito.android.tech_budget.internal.owners.mapper.TechBudgetNoOpMapper
import com.avito.android.tech_budget.owners.TechBudgetOwner
import com.avito.android.tech_budget.owners.TechBudgetOwnerMapper
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class CollectOwnersConfiguration @Inject constructor(
    objects: ObjectFactory,
) {

    /**
     * A separate mapper to convert data from Owner to TechBudgetOwner
     * with required fields without modifying the OwnerAdapter itself.
     *
     * The mapper takes [Owner] instance as input and expects [TechBudgetOwner] as output
     */
    @get:Input
    public val techBudgetOwnerMapper: Property<TechBudgetOwnerMapper> = objects.property<TechBudgetOwnerMapper>()
        .convention(TechBudgetNoOpMapper())
}
