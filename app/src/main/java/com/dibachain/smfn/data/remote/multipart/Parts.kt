// data/remote/multipart/Parts.kt
package com.dibachain.smfn.data.remote.multipart

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.buffer
import okio.source

fun String.asRb() = RequestBody.create("text/plain".toMediaTypeOrNull(), this)

fun jsonArrayOfStrings(values: List<String>) =
    ("[" + values.joinToString(",") { "\"${it}\"" } + "]").asRb()

private fun guessMime(name: String): String = when {
    name.endsWith(".png", true) -> "image/png"
    name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) -> "image/jpeg"
    name.endsWith(".webp", true) -> "image/webp"
    name.endsWith(".mp4", true) -> "video/mp4"
    else -> "application/octet-stream"
}

fun uriToPart(
    ctx: Context,
    uri: Uri,
    formName: String,
    fileName: String? = null
): MultipartBody.Part {
    val resolver = ctx.contentResolver
    val name = fileName ?: (runCatching {
        resolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
    }.getOrNull() ?: "file")

    val mime = runCatching { resolver.getType(uri) }.getOrNull() ?: guessMime(name)
    val rb = object : RequestBody() {
        override fun contentType() = mime.toMediaTypeOrNull()
        override fun writeTo(sink: okio.BufferedSink) {
            resolver.openInputStream(uri)?.source()?.buffer().use { src ->
                if (src != null) sink.writeAll(src)
            }
        }
    }
    return MultipartBody.Part.createFormData(formName, name, rb)
}

fun urisToParts(ctx: Context, uris: List<Uri>, formName: String): List<MultipartBody.Part> =
    uris.mapIndexed { idx, u -> uriToPart(ctx, u, formName, "img_$idx") }
