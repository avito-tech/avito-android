package com.avito.android.tech_budget.internal.owners.mapper

import com.avito.android.model.Owner
import com.avito.android.tech_budget.owners.TechBudgetOwner
import com.avito.android.tech_budget.owners.TechBudgetOwnerMapper

internal class TechBudgetNoOpMapper : TechBudgetOwnerMapper {

    override fun map(owner: Owner): TechBudgetOwner {
        val message = "Mapping not implemented at TechBudgetNoOpMapper. " +
            "Please, provide implementation of TechBudgetOwnerMapper to `techBudgetOwnerMapper` property."
        error(message)
    }
}
