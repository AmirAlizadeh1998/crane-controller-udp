package com.tivanco.hymaco.utils.fileHandler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap

fun openFile(context: Context, uri: Uri, mime: String?) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime ?: getMimeFromUri(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun getMimeFromUri(uri: Uri): String? =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(
        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    )
