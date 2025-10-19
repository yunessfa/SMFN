package com.dibachain.smfn.data.remote

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

private fun queryDisplayName(cr: ContentResolver, uri: Uri): String? {
    var name: String? = null
    val c: Cursor? = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    c?.use { if (it.moveToFirst()) name = it.getString(0) }
    return name
}

/** RequestBody استریم‌محور با کال‌بک پیشرفت */
private class InputStreamProgressRequestBody(
    private val cr: ContentResolver,
    private val uri: Uri,
    private val mime: String?,
    private val onProgress: (percent: Int) -> Unit
) : RequestBody() {
    override fun contentType() = mime?.toMediaTypeOrNull()
    override fun writeTo(sink: BufferedSink) {
        val total = runCatching { cr.openAssetFileDescriptor(uri, "r")?.length ?: -1L }.getOrDefault(-1L)
        cr.openInputStream(uri)?.use { input ->
            input.source().use { source ->
                var written = 0L
                var lastEmit = -1
                while (true) {
                    val read = source.read(sink.buffer, 8 * 1024)
                    if (read == -1L) break
                    sink.emit()
                    written += read
                    if (total > 0) {
                        val p = ((written * 100) / total).toInt().coerceIn(0, 100)
                        if (p != lastEmit) { onProgress(p); lastEmit = p }
                    }
                }
                if (total <= 0) onProgress(100)
            }
        }
    }
}

/** ساخت Part با کلید "image" و گزارش پیشرفت */
fun ContentResolver.uriToImagePartWithProgress(
    uri: Uri,
    partName: String = "image",
    onProgress: (Int) -> Unit
): MultipartBody.Part {
    val mime = getType(uri) ?: "image/*"
    val fileName = queryDisplayName(this, uri) ?: "image_${System.currentTimeMillis()}.jpg"
    val body = InputStreamProgressRequestBody(this, uri, mime, onProgress)
    return MultipartBody.Part.createFormData(partName, fileName, body)
}





/** ویدئو: partName = "video" */
fun ContentResolver.uriToVideoPartWithProgress(
    uri: Uri,
    partName: String = "video",
    onProgress: (Int) -> Unit
): MultipartBody.Part {
    val mime = getType(uri) ?: "video/*"
    val fileName = queryDisplayName(this, uri) ?: "video_${System.currentTimeMillis()}.mp4"
    val body = InputStreamProgressRequestBody(this, uri, mime, onProgress)
    return MultipartBody.Part.createFormData(partName, fileName, body)
}
