package com.avito.android.tech_budget.owners

import com.avito.android.model.Owner

public fun interface TechBudgetOwnerMapper {

    public fun map(owner: Owner): TechBudgetOwner
}
