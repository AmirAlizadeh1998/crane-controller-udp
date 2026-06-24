package com.tivanco.hymaco.utils.fileHandler

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object Utils {
    fun createImageUri(context: Context): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }
}
