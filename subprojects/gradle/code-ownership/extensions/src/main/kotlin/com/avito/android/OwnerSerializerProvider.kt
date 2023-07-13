package com.avito.android

public interface OwnerSerializerProvider {

    public fun provideIdSerializer(): OwnerIdSerializer

    public fun provideNameSerializer(): OwnerNameSerializer
}
