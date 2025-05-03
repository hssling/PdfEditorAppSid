package com.example.pdfeditor

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.FileOutputStream

fun mergePdfs(context: Context, uris: List<Uri>): File {
    val merged = PDDocument()
    for (uri in uris) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_merge_${System.currentTimeMillis()}.pdf")
        val out = FileOutputStream(tempFile)
        inputStream?.copyTo(out)
        inputStream?.close()
        out.close()
        val doc = PDDocument.load(tempFile)
        for (page in doc.pages) {
            merged.addPage(page)
        }
        doc.close()
        tempFile.delete()
    }
    val outFile = File(context.cacheDir, "merged_output.pdf")
    merged.save(outFile)
    merged.close()
    return outFile
}

fun splitPdf(context: Context, uri: Uri, fromPage: Int, toPage: Int): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_split.pdf")
    val out = FileOutputStream(file)
    inputStream?.copyTo(out)
    inputStream?.close()
    out.close()
    val doc = PDDocument.load(file)
    val splitDoc = PDDocument()
    for (i in fromPage..toPage) {
        splitDoc.addPage(doc.getPage(i))
    }
    val outFile = File(context.cacheDir, "split_output.pdf")
    splitDoc.save(outFile)
    splitDoc.close()
    doc.close()
    file.delete()
    return outFile
}
