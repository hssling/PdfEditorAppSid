package com.example.pdfeditor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.File
import java.io.FileOutputStream

fun addImageToPdfPage(
    context: Context,
    uri: Uri,
    pageIndex: Int,
    image: Bitmap,
    posX: Float = 50f,
    posY: Float = 500f,
    scale: Float = 1.0f
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_img_edit_out.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    val page = document.getPage(pageIndex)
    val pdImage = LosslessFactory.createFromImage(document, image)
    val contentStream = PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)
    contentStream.drawImage(pdImage, posX, posY, image.width * scale, image.height * scale)
    contentStream.close()
    document.save(file)
    document.close()
    return file
}

fun clearPageContent(
    context: Context,
    uri: Uri,
    pageIndex: Int
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_img_clear_out.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    val page = document.getPage(pageIndex)
    page.contents = null // Remove all content
    document.save(file)
    document.close()
    return file
}
