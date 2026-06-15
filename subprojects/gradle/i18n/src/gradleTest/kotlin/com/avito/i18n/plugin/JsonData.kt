package com.avito.i18n.plugin

import org.intellij.lang.annotations.Language

@Language("JSON")
val RESPONSE_BODY = """
    {
        "result": {
            "status": {
                "code": "ok",
                "message": "some message"
            },
            "data": {
                "task": {
                    "taskUUID": "Some task id",
                    "namespace": "android",
                    "contentType": "avito",
                    "status": "success",
                    "description": "some description"
                },
                "textUnits": [
                    {
                        "status": 10,
                        "unit": {
                            "key": "some_string",
                            "component": "some component",
                            "namespace": "android",
                            "status": 1,
                            "translated": [
                                {
                                    "lang": "en",
                                    "text": {
                                        "text": "emos gnirts"
                                    }
                                }
                            ]
                        }
                    },
                    {
                        "status": 10,
                        "unit": {
                            "key": "params_string",
                            "component": "some component",
                            "namespace": "android",
                            "status": 1,
                            "translated": [
                                {
                                    "lang": "en",
                                    "text": {
                                        "text": "smarap %s gnirts %d"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    }
    """.trimIndent()

@Language("JSON")
val RESPONSE_BODY_TRANSLATABLE_FALSE = """
    {
        "result": {
            "status": {
                "code": "ok",
                "message": "some message"
            },
            "data": {
                "task": {
                    "taskUUID": "Some task id",
                    "namespace": "android",
                    "contentType": "avito",
                    "status": "success",
                    "description": "some description"
                },
                "textUnits": [
                    {
                        "status": 10,
                        "unit": {
                            "key": "params_string",
                            "component": "some component",
                            "namespace": "android",
                            "status": 1,
                            "translated": [
                                {
                                    "lang": "en",
                                    "text": {
                                        "text": "smarap %s gnirts %d"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    }
    """.trimIndent()

@Language("JSON")
val RESPONSE_BODY_WITH_MARKUP = """
    {
        "result": {
            "status": {
                "code": "ok",
                "message": "some message"
            },
            "data": {
                "task": {
                    "taskUUID": "Some task id",
                    "namespace": "android",
                    "contentType": "avito",
                    "status": "success",
                    "description": "some description"
                },
                "textUnits": [
                    {
                        "status": 10,
                        "unit": {
                            "key": "underlined_text",
                            "component": "some component",
                            "namespace": "android",
                            "status": 1,
                            "translated": [
                                {
                                    "lang": "en",
                                    "text": {
                                        "text": "<u>More</u>"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    }
    """.trimIndent()

@Language("JSON")
val RESPONSE_BODY_WITH_CDATA = """
    {
        "result": {
            "status": {
                "code": "ok",
                "message": "some message"
            },
            "data": {
                "task": {
                    "taskUUID": "Some task id",
                    "namespace": "android",
                    "contentType": "avito",
                    "status": "success",
                    "description": "some description"
                },
                "textUnits": [
                    {
                        "status": 10,
                        "unit": {
                            "key": "html_text",
                            "component": "some component",
                            "namespace": "android",
                            "status": 1,
                            "translated": [
                                {
                                    "lang": "en",
                                    "text": {
                                        "text": "<b>Matn</b>"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    }
    """.trimIndent()

@Language("JSON")
val RESPONSE_BODY_CHANGE_STRING = """
    {
        "result": {
            "status": {
                "code": "ok",
                "message": "some message"
            },
            "data": {
                "task": {
                    "taskUUID": "Some task id",
                    "namespace": "android",
                    "contentType": "avito",
                    "status": "success",
                    "description": "some description"
                },
                "textUnits": [
                    {
                        "status": 10,
                        "unit": {
                            "key": "some_string",
                            "component": "some component",
                            "namespace": "android",
                            "status": 1,
                            "translated": [
                                {
                                    "lang": "en",
                                    "text": {
                                        "text": "emos gnirts 321"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    }
    """.trimIndent()
