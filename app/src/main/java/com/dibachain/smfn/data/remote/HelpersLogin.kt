// data/remote/parts.kt
package com.dibachain.smfn.data.remote

import com.dibachain.smfn.core.Public
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private val TEXT = "text/plain".toMediaType()
fun textPart(value: String): RequestBody = value.toRequestBody(TEXT)

fun String.asPart(): RequestBody = toRequestBody(TEXT)
fun String.toFullImageUrl(): String =
    if (startsWith("/")) Public.BASE_URL_IMAGE + trimStart('/') else this