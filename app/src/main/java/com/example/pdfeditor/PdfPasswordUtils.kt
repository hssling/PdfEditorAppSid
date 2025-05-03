package com.example.pdfeditor

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.FileOutputStream

fun setPdfPassword(context: Context, uri: Uri, password: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_password.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    document.protect(
        com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy(
            password,
            password,
            com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission()
        ).apply {
            encryptionKeyLength = 128
            permissions.setCanPrint(true)
            permissions.setCanModify(true)
        }
    )
    document.save(file)
    document.close()
    return file
}

fun removePdfPassword(context: Context, uri: Uri, password: String): File? {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_unprotected.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return try {
        val document = PDDocument.load(file, password)
        document.setAllSecurityToBeRemoved(true)
        document.save(file)
        document.close()
        file
    } catch (e: Exception) {
        null
    }
}
