package com.dicoding.cancer.helper

import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun getImageUri(bitmap: Bitmap): Uri {
    val tempFile = File.createTempFile("temp_image", ".png")
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
    val bitmapData = bytes.toByteArray()
    val fileOutPut = FileOutputStream(tempFile)
    fileOutPut.write(bitmapData)
    fileOutPut.flush()
    fileOutPut.close()
    return Uri.fromFile(tempFile)
}