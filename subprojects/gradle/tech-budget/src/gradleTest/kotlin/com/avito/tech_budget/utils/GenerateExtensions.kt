package com.avito.tech_budget.utils

internal fun dumpInfoExtension(webServerUrl: String): String = """
                    dumpInfo { 
                        baseUploadUrl.set("$webServerUrl")
                        commitHash.set("123")
                        currentDate.set("2022-10-31")
                        project.set("avito")
                    }    
""".trimIndent()
