package com.avito.android.owner.adapter

import com.avito.android.OwnerIdSerializer
import com.avito.android.OwnerNameSerializer
import com.avito.android.OwnerNoOpSerializer
import com.avito.android.OwnerSerializer

public class OwnerAdapterFactory(
    private val serializer: OwnerSerializer = OwnerNoOpSerializer
) {

    public val adapter: OwnerAdapter = createAdapter()

    private fun createAdapter(): OwnerAdapter {
        return when (serializer) {
            is OwnerIdSerializer -> OwnerIdAdapter(serializer)
            is OwnerNameSerializer -> OwnerNameAdapter(serializer)
            is OwnerNoOpSerializer -> OwnerNoOpAdapter()
        }
    }
}
