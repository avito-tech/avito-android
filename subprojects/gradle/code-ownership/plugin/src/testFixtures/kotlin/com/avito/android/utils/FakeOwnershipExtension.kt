package com.avito.android.utils

import org.intellij.lang.annotations.Language

@Language("kts")
val FAKE_OWNERSHIP_EXTENSION: String =
    """
            public enum class FakeOwners(val id: String): com.avito.android.model.Owner {
                Speed("SpeedID"),
                Messenger("MessengerID")
            }
        
            public class FakeOwnerNameSerializer : com.avito.android.OwnerNameSerializer {
                    override fun deserialize(rawOwner: String): com.avito.android.model.Owner {
                        return FakeOwners.valueOf(rawOwner)
                    }
                
                    override fun serialize(owner: com.avito.android.model.Owner): String {
                        return (owner as? FakeOwners)?.name ?: owner.toString()
                    }
            }
        
            public class FakeOwnersIdSerializer : com.avito.android.OwnerIdSerializer {
                    override fun deserialize(ownerId: String): com.avito.android.model.Owner {
                        return FakeOwners.values().first { it.id == ownerId }
                    }
                
                    override fun serialize(owner: com.avito.android.model.Owner): List<String> {
                        return listOf((owner as? FakeOwners)?.id ?: owner.toString())
                    }
            }
            
            public class FakeOwnerSerializersProvider: com.avito.android.OwnerSerializerProvider {
                
                    override fun provideIdSerializer(): com.avito.android.OwnerIdSerializer = FakeOwnersIdSerializer()
                    override fun provideNameSerializer(): com.avito.android.OwnerNameSerializer = 
                            FakeOwnerNameSerializer()
            
            }

            ownership {
                owners(FakeOwners.Speed, FakeOwners.Messenger)
                ownerSerializersProvider.set(FakeOwnerSerializersProvider())
            }
        """.trimIndent()

@Language("kts")
val FAKE_OWNERS_PROVIDER_EXTENSION = """
            codeOwnershipDiffReport {
                expectedOwnersProvider.set(
                    com.avito.android.diff.provider.OwnersProvider {
                        setOf(FakeOwners.Speed, FakeOwners.Messenger)                       
                    }
                )
            }
    """.trimIndent()
