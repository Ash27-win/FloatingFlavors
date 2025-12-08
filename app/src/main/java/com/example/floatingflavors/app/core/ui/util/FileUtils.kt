package com.example.floatingflavors.app.core.ui.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

/**
 * Convert a content Uri (gallery/camera) into a temporary File stored in cacheDir.
 * Safe for all API levels. Caller should run this off the UI thread (e.g. viewModelScope or IO dispatcher).
 */
fun uriToFile(context: Context, uri: Uri, destFileName: String): File {
    val input: InputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Cannot open URI")
    val tempFile = File(context.cacheDir, destFileName)
    tempFile.outputStream().use { out ->
        input.copyTo(out)
    }
    input.close()
    return tempFile
}