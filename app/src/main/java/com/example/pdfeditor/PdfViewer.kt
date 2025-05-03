package com.example.pdfeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfViewer(
    uri: Uri?,
    initialPage: Int = 0,
    onEdit: (Int) -> Unit = {},
    onEditImage: (Int) -> Unit = {},
    onEditAnnotation: (Int) -> Unit = {},
    onEditText: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var pageCount by remember { mutableStateOf(0) }
    var pageIndex by remember { mutableStateOf(initialPage) }
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri, pageIndex) {
        uri?.let {
            val file = getFileFromUri(context, it)
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            pageCount = renderer.pageCount
            if (renderer.pageCount > 0 && pageIndex in 0 until renderer.pageCount) {
                val page = renderer.openPage(pageIndex)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmapState.value = bitmap
                page.close()
            }
            renderer.close()
            fileDescriptor.close()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.padding(8.dp)) {
            Button(onClick = { if (pageIndex > 0) pageIndex-- }, enabled = pageIndex > 0) {
                Text("Previous")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Page ${'$'}{pageIndex + 1} of ${'$'}pageCount")
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { if (pageIndex < pageCount - 1) pageIndex++ }, enabled = pageIndex < pageCount - 1) {
                Text("Next")
            }
        }
        bitmapState.value?.let { bmp ->
            Image(bitmap = bmp.asImageBitmap(), contentDescription = "PDF Page", modifier = Modifier.fillMaxWidth().padding(8.dp))
        }
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = { onEditText(pageIndex) }, modifier = Modifier.padding(end = 8.dp)) {
                Text("Edit Text")
            }
            Button(onClick = { onEditImage(pageIndex) }, modifier = Modifier.padding(end = 8.dp)) {
                Text("Edit Images")
            }
            Button(onClick = { onEditAnnotation(pageIndex) }) {
                Text("Edit Annotations")
            }
        }
    }
}

fun getFileFromUri(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp.pdf")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}
