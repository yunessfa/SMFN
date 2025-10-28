// data/remote/MultipartExt.kt
package com.dibachain.smfn.data.remote

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

fun String.asTextPart(): RequestBody =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

fun Uri.asImagePart(
    contentResolver: ContentResolver,
    partName: String = "image",
    fileName: String = "upload.jpg"
): MultipartBody.Part {
    // محتوا را به فایل temp بریز (سازگار با SAF)
    val input = contentResolver.openInputStream(this)!!
    val temp = File.createTempFile("upload_", "_img")
    FileOutputStream(temp).use { out -> input.copyTo(out) }
    val body = temp.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, fileName, body)
}
