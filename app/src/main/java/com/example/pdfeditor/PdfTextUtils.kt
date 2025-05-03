package com.example.pdfeditor

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream

fun extractTextFromPdfPage(context: Context, uri: Uri, pageIndex: Int): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_edit.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    val stripper = PDFTextStripper()
    stripper.startPage = pageIndex + 1
    stripper.endPage = pageIndex + 1
    val text = stripper.getText(document)
    document.close()
    return text
}

fun replaceTextOnPdfPage(
    context: Context,
    uri: Uri,
    pageIndex: Int,
    newText: String,
    fontSize: Float = 12f,
    posX: Float = 50f,
    posY: Float = 700f
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_edit_out.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    // WARNING: This is a placeholder. Real text replacement in PDFs is complex and may not preserve layout.
    val page = document.getPage(pageIndex)
    // Remove all content from the page
    page.contents = null
    // Add new text (with chosen position and font size)
    val contentStream = com.tom_roush.pdfbox.pdmodel.PDPageContentStream(document, page)
    contentStream.beginText()
    contentStream.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, fontSize)
    contentStream.newLineAtOffset(posX, posY)
    contentStream.showText(newText)
    contentStream.endText()
    contentStream.close()
    document.save(file)
    document.close()
    return file
}
