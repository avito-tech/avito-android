package com.avito.android

import com.avito.android.serializers.OwnerIdSerializer
import com.avito.android.serializers.OwnerNameSerializer

public interface OwnerSerializerProvider {

    public fun provideIdSerializer(): OwnerIdSerializer

    public fun provideNameSerializer(): OwnerNameSerializer
}
