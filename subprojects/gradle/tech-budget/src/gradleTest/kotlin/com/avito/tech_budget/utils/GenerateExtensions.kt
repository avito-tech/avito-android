package com.avito.tech_budget.utils

internal fun dumpInfoExtension(webServerUrl: String): String = """
                    dumpInfo { 
                        baseUploadUrl.set("$webServerUrl")
                        commitHash.set("123")
                        currentDate.set("2022-10-31")
                        project.set("avito")
                    }    
""".trimIndent()

internal fun ownershipExtension(): String =
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
                        return (owner as FakeOwners).name
                    }
            }

            ownership {
                owners(FakeOwners.Speed, FakeOwners.Messenger)
                ownerSerializer.set(FakeOwnersSerializer())
            }
        """.trimIndent()
