package com.avito.retrofit.adapter

public class HttpException(public val code: Int, public val body: String) : RuntimeException()
