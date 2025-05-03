package com.example.pdfeditor

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationText
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.io.File
import java.io.FileOutputStream

fun addHighlightAnnotation(
    context: Context,
    uri: Uri,
    pageIndex: Int,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Int = Color.YELLOW
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_annot_highlight.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    val page = document.getPage(pageIndex)
    val highlight = PDAnnotationHighlight()
    highlight.rectangle = PDRectangle(x, y, width, height)
    highlight.color = com.tom_roush.pdfbox.pdmodel.common.PDColor(floatArrayOf(
        Color.red(color) / 255f,
        Color.green(color) / 255f,
        Color.blue(color) / 255f
    ), com.tom_roush.pdfbox.pdmodel.common.PDDeviceRGB.INSTANCE)
    page.annotations.add(highlight)
    document.save(file)
    document.close()
    return file
}

fun addTextAnnotation(
    context: Context,
    uri: Uri,
    pageIndex: Int,
    x: Float,
    y: Float,
    text: String
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_annot_text.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    val page = document.getPage(pageIndex)
    val annot = PDAnnotationText()
    annot.rectangle = PDRectangle(x, y, 30f, 30f)
    annot.contents = text
    page.annotations.add(annot)
    document.save(file)
    document.close()
    return file
}

fun addShapeAnnotation(
    context: Context,
    uri: Uri,
    pageIndex: Int,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    shapeType: String = "rectangle",
    color: Int = Color.RED
): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_annot_shape.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val document = PDDocument.load(file)
    val page = document.getPage(pageIndex)
    val annot = PDAnnotationSquareCircle()
    annot.rectangle = PDRectangle(x, y, width, height)
    annot.color = com.tom_roush.pdfbox.pdmodel.common.PDColor(floatArrayOf(
        Color.red(color) / 255f,
        Color.green(color) / 255f,
        Color.blue(color) / 255f
    ), com.tom_roush.pdfbox.pdmodel.common.PDDeviceRGB.INSTANCE)
    annot.subtype = if (shapeType == "ellipse") PDAnnotationSquareCircle.SUB_TYPE_CIRCLE else PDAnnotationSquareCircle.SUB_TYPE_SQUARE
    page.annotations.add(annot)
    document.save(file)
    document.close()
    return file
}
