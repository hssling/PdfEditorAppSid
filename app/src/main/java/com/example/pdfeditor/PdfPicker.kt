package com.example.pdfeditor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun PdfPicker(onPdfPicked: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> onPdfPicked(uri) }
    )
    Button(onClick = {
        launcher.launch(arrayOf("application/pdf"))
    }) {
        Text("Pick PDF File")
    }
}
