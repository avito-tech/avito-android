package com.avito.android.utils

import org.intellij.lang.annotations.Language

@Language("kts")
val FAKE_OWNERSHIP_EXTENSION: String =
    """
            public enum class FakeOwners: com.avito.android.model.Owner {
                Speed,
                Messenger
            }
        
            public class FakeOwnersSerializer : com.avito.android.OwnerSerializer {
                    override fun deserialize(rawOwner: String): com.avito.android.model.Owner {
                        return FakeOwners.valueOf(rawOwner)
                    }
                
                    override fun serialize(owner: com.avito.android.model.Owner): String {
                        return (owner as? FakeOwners)?.name ?: owner.toString()
                    }
            }

            ownership {
                owners(FakeOwners.Speed, FakeOwners.Messenger)
                ownerSerializer.set(FakeOwnersSerializer())
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
