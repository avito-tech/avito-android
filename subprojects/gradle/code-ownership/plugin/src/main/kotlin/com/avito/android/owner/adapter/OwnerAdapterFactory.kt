package com.avito.android.owner.adapter

import com.avito.android.serializers.OwnerFieldSerializer
import com.avito.android.serializers.OwnerIdSerializer
import com.avito.android.serializers.OwnerNameSerializer
import com.avito.android.serializers.OwnerNoOpSerializer

public class OwnerAdapterFactory(
    private val serializer: OwnerFieldSerializer = OwnerNoOpSerializer
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
