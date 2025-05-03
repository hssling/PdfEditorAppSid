package com.example.pdfeditor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.File
import java.io.FileOutputStream

fun bitmapsToPdf(context: Context, bitmaps: List<Bitmap>): File {
    val file = File(context.cacheDir, "scanned_output.pdf")
    val document = PDDocument()
    for (bitmap in bitmaps) {
        val page = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
        document.addPage(page)
        val pdImage = LosslessFactory.createFromImage(document, bitmap)
        val contentStream = PDPageContentStream(document, page)
        contentStream.drawImage(pdImage, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        contentStream.close()
    }
    document.save(file)
    document.close()
    return file
}
