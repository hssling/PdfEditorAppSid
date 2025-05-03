package com.example.pdfeditor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.pdfeditor.ui.theme.PdfEditorAppTheme
import com.example.pdfeditor.extractTextFromPdfPage
import com.example.pdfeditor.replaceTextOnPdfPage
import com.example.pdfeditor.addImageToPdfPage
import com.example.pdfeditor.clearPageContent
import com.example.pdfeditor.addHighlightAnnotation
import com.example.pdfeditor.addTextAnnotation
import com.example.pdfeditor.addShapeAnnotation
import com.example.pdfeditor.setPdfPassword
import com.example.pdfeditor.removePdfPassword
import com.example.pdfeditor.addSignatureToPdfPage
import com.example.pdfeditor.mergePdfs
import com.example.pdfeditor.splitPdf
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfEditorAppMain()
        }
    }
}

enum class EditMode { TEXT, IMAGE, ANNOTATION, SCAN_TO_PDF }

@Composable
fun PdfEditorAppMain() {
    var showSettings by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showBatch by remember { mutableStateOf(false) }
    var darkTheme by remember { mutableStateOf(false) }
    var defaultFontSize by remember { mutableStateOf(16f) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    PdfEditorAppTheme(darkTheme = darkTheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column {
                    NavigationDrawerItem(
                        label = { Text("Home", fontSize = defaultFontSize.sp) },
                        selected = !showSettings && !showHelp && !showBatch,
                        onClick = { showSettings = false; showHelp = false; showBatch = false },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings", fontSize = defaultFontSize.sp) },
                        selected = showSettings,
                        onClick = { showSettings = true },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Batch Processing", fontSize = defaultFontSize.sp) },
                        selected = showBatch,
                        onClick = { showBatch = true },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Help/About", fontSize = defaultFontSize.sp) },
                        selected = showHelp,
                        onClick = { showHelp = true },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        ) {
            Scaffold { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when {
                        showSettings -> SettingsScreen(
                            darkTheme = darkTheme,
                            onThemeChange = { darkTheme = it },
                            defaultFontSize = defaultFontSize,
                            onFontSizeChange = { defaultFontSize = it }
                        )
                        showHelp -> HelpScreen(defaultFontSize)
                        showBatch -> BatchProcessingScreen(defaultFontSize)
                        else -> MainActivityScreen(defaultFontSize)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    defaultFontSize: Float,
    onFontSizeChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontSize = defaultFontSize.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Theme:", fontSize = defaultFontSize.sp)
            Switch(checked = darkTheme, onCheckedChange = onThemeChange)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Default Font Size:", fontSize = defaultFontSize.sp)
            Slider(value = defaultFontSize, onValueChange = onFontSizeChange, valueRange = 8f..36f, steps = 7, modifier = Modifier.weight(1f))
            Text("${'$'}defaultFontSize", modifier = Modifier.padding(start = 8.dp), fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun HelpScreen(defaultFontSize: Float) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Help & About", style = MaterialTheme.typography.titleLarge, fontSize = defaultFontSize.sp)
        Text("\n- Open, edit, and convert PDFs\n- Use the navigation menu for more features\n- Edit text, images, annotations\n- Scan documents to PDF\n- Export or share your PDFs\n- Batch process multiple PDFs\n- Set password protection and digital signatures\n\nFor more help, visit our website or contact support.", fontSize = defaultFontSize.sp)
    }
}

@Composable
fun BatchProcessingScreen(defaultFontSize: Float) {
    val context = LocalContext.current
    var selectedUris by remember { mutableStateOf(listOf<Uri>()) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var fromPage by remember { mutableStateOf(0) }
    var toPage by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val pdfPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedUris = uris ?: emptyList()
    }
    val singlePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { selectedUris = listOf(it) }
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Batch Processing", style = MaterialTheme.typography.titleLarge, fontSize = defaultFontSize.sp)
        Row {
            Button(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }, modifier = Modifier.padding(end = 8.dp)) {
                Text("Pick PDFs to Merge", fontSize = defaultFontSize.sp)
            }
            Button(onClick = { singlePickerLauncher.launch(arrayOf("application/pdf")) }) {
                Text("Pick PDF to Split", fontSize = defaultFontSize.sp)
            }
        }
        if (selectedUris.size > 1) {
            Button(onClick = {
                loading = true
                try {
                    resultFile = mergePdfs(context, selectedUris)
                } catch (e: Exception) {
                    errorMsg = "Error merging PDFs: ${'$'}{e.localizedMessage}"
                } finally {
                    loading = false
                }
            }, modifier = Modifier.padding(top = 8.dp), enabled = !loading) {
                Text(if (loading) "Merging..." else "Merge PDFs", fontSize = defaultFontSize.sp)
            }
        }
        if (selectedUris.size == 1) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Text("From page:", fontSize = defaultFontSize.sp)
                TextField(value = fromPage.toString(), onValueChange = { fromPage = it.toIntOrNull() ?: 0 }, modifier = Modifier.width(60.dp), fontSize = defaultFontSize.sp)
                Text("To page:", fontSize = defaultFontSize.sp)
                TextField(value = toPage.toString(), onValueChange = { toPage = it.toIntOrNull() ?: 0 }, modifier = Modifier.width(60.dp), fontSize = defaultFontSize.sp)
                Button(onClick = {
                    loading = true
                    try {
                        resultFile = splitPdf(context, selectedUris[0], fromPage, toPage)
                    } catch (e: Exception) {
                        errorMsg = "Error splitting PDF: ${'$'}{e.localizedMessage}"
                    } finally {
                        loading = false
                    }
                }, modifier = Modifier.padding(start = 8.dp), enabled = !loading) {
                    Text(if (loading) "Splitting..." else "Split PDF", fontSize = defaultFontSize.sp)
                }
            }
        }
        if (resultFile != null) {
            Text("Result PDF: ${'$'}{resultFile?.absolutePath}", modifier = Modifier.padding(top = 8.dp), fontSize = defaultFontSize.sp)
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
        }
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun MainActivityScreen(defaultFontSize: Float) {
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var editPageIndex by remember { mutableStateOf<Int?>(null) }
    var editMode by remember { mutableStateOf<EditMode?>(null) }
    var editedPdfFile by remember { mutableStateOf<File?>(null) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    if (pickedUri == null) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            PdfPicker(onPdfPicked = { uri -> pickedUri = uri }, modifier = Modifier.semantics { contentDescription = "Pick a PDF file" })
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { editMode = EditMode.SCAN_TO_PDF }, modifier = Modifier.semantics { contentDescription = "Scan to PDF" }) { Text("Scan to PDF", fontSize = defaultFontSize.sp) }
            if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    } else if (editPageIndex != null && editMode != null) {
        try {
            when (editMode) {
                EditMode.TEXT -> EditPdfPageScreen(
                    uri = pickedUri!!,
                    pageIndex = editPageIndex!!,
                    onDone = { newFile ->
                        if (newFile != null) {
                            editedPdfFile = newFile
                            pickedUri = Uri.fromFile(newFile)
                        }
                        editPageIndex = null
                        editMode = null
                    }
                )
                EditMode.IMAGE -> EditPdfImagesScreen(
                    uri = pickedUri!!,
                    pageIndex = editPageIndex!!,
                    onDone = { editPageIndex = null; editMode = null }
                )
                EditMode.ANNOTATION -> EditPdfAnnotationsScreen(
                    uri = pickedUri!!,
                    pageIndex = editPageIndex!!,
                    onDone = { editPageIndex = null; editMode = null }
                )
                else -> {}
            }
        } catch (e: Exception) {
            errorMsg = "Error editing PDF: ${'$'}{e.localizedMessage}" 
            editPageIndex = null
            editMode = null
        }
    } else if (editMode == EditMode.SCAN_TO_PDF) {
        ScanToPdfScreen(onDone = { uri ->
            if (uri != null) pickedUri = uri
            editMode = null
        })
    } else {
        Column {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
            }
            PdfViewer(
                uri = pickedUri,
                onEdit = { page -> editPageIndex = page },
                onEditImage = { page -> editPageIndex = page; editMode = EditMode.IMAGE },
                onEditAnnotation = { page -> editPageIndex = page; editMode = EditMode.ANNOTATION },
                onEditText = { page -> editPageIndex = page; editMode = EditMode.TEXT },
                modifier = Modifier.semantics { contentDescription = "PDF viewer" }
            )
            ExportShareBar(currentPdfFile = editedPdfFile)
            PasswordProtectionBar(currentPdfFile = editedPdfFile, onPasswordChanged = { editedPdfFile = it })
            SignatureBar(currentPdfFile = editedPdfFile, pageIndex = currentPageIndex, onSigned = { editedPdfFile = it })
            if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun EditPdfPageScreen(
    uri: Uri,
    pageIndex: Int,
    onDone: (File?) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(12f) }
    var posX by remember { mutableStateOf(50f) }
    var posY by remember { mutableStateOf(700f) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!isLoaded) {
            loading = true
            try {
                text = extractTextFromPdfPage(context, uri, pageIndex)
            } catch (e: Exception) {
                errorMsg = "Error loading PDF page: ${'$'}{e.localizedMessage}"
            } finally {
                isLoaded = true
                loading = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Edit Text: Page ${'$'}{pageIndex + 1}", style = MaterialTheme.typography.titleLarge)
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
        } else {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.padding(vertical = 8.dp).weight(1f, fill = false),
                label = { Text("Page Text") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Font Size:", modifier = Modifier.padding(end = 8.dp))
                Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 8f..36f, steps = 7, modifier = Modifier.weight(1f))
                Text("${'$'}fontSize", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("X:")
                Slider(value = posX, onValueChange = { posX = it }, valueRange = 0f..400f, steps = 39, modifier = Modifier.weight(1f))
                Text("Y:")
                Slider(value = posY, onValueChange = { posY = it }, valueRange = 0f..800f, steps = 79, modifier = Modifier.weight(1f))
            }
            Button(onClick = {
                isSaving = true
                loading = true
                try {
                    val newFile = replaceTextOnPdfPage(context, uri, pageIndex, text, fontSize, posX, posY)
                    onDone(newFile)
                } catch (e: Exception) {
                    errorMsg = "Error saving PDF page: ${'$'}{e.localizedMessage}"
                } finally {
                    isSaving = false
                    loading = false
                }
            }, enabled = !isSaving && !loading) {
                Text(if (isSaving || loading) "Saving..." else "Save and Exit", fontSize = defaultFontSize.sp)
            }
            Button(onClick = { onDone(null) }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Cancel", fontSize = defaultFontSize.sp)
            }
        }
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun EditPdfImagesScreen(uri: Uri, pageIndex: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var posX by remember { mutableStateOf(50f) }
    var posY by remember { mutableStateOf(500f) }
    var scale by remember { mutableStateOf(1.0f) }
    var isSaving by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uriResult ->
        if (uriResult != null) {
            val bmp = androidx.compose.ui.res.loadImageBitmap(context.contentResolver.openInputStream(uriResult)!!)
            imageBitmap = bmp.asAndroidBitmap()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Edit Images: Page ${'$'}{pageIndex + 1}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { imagePickerLauncher.launch("image/*") }) { Text("Pick Image to Add", fontSize = defaultFontSize.sp) }
        imageBitmap?.let { bmp ->
            Image(
                bitmap = asImageBitmap(bmp),
                contentDescription = "Selected Image",
                modifier = Modifier.size(120.dp).padding(vertical = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scale:", modifier = Modifier.padding(end = 8.dp))
                Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.1f..2.0f, steps = 19, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("X:")
                Slider(value = posX, onValueChange = { posX = it }, valueRange = 0f..400f, steps = 39, modifier = Modifier.weight(1f))
                Text("Y:")
                Slider(value = posY, onValueChange = { posY = it }, valueRange = 0f..800f, steps = 79, modifier = Modifier.weight(1f))
            }
            Button(onClick = {
                isSaving = true
                loading = true
                try {
                    val newFile = addImageToPdfPage(context, uri, pageIndex, bmp, posX, posY, scale)
                    // Replace the PDF with the new file (optional: you can pass this back to main)
                    isSaving = false
                    loading = false
                    onDone()
                } catch (e: Exception) {
                    errorMsg = "Error adding image to PDF: ${'$'}{e.localizedMessage}"
                    isSaving = false
                    loading = false
                }
            }, enabled = !isSaving && !loading) {
                Text(if (isSaving || loading) "Adding..." else "Add Image to Page", fontSize = defaultFontSize.sp)
            }
        }
        Button(onClick = {
            isSaving = true
            loading = true
            try {
                val newFile = clearPageContent(context, uri, pageIndex)
                isSaving = false
                loading = false
                onDone()
            } catch (e: Exception) {
                errorMsg = "Error clearing page content: ${'$'}{e.localizedMessage}"
                isSaving = false
                loading = false
            }
        }, modifier = Modifier.padding(top = 8.dp), enabled = !isSaving && !loading) {
            Text(if (isSaving || loading) "Removing..." else "Remove All Images/Content", fontSize = defaultFontSize.sp)
        }
        Button(onClick = onDone, modifier = Modifier.padding(top = 16.dp)) { Text("Done", fontSize = defaultFontSize.sp) }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
        }
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun EditPdfAnnotationsScreen(uri: Uri, pageIndex: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    var annotationType by remember { mutableStateOf("highlight") }
    var x by remember { mutableStateOf(50f) }
    var y by remember { mutableStateOf(500f) }
    var width by remember { mutableStateOf(100f) }
    var height by remember { mutableStateOf(30f) }
    var color by remember { mutableStateOf(android.graphics.Color.YELLOW) }
    var commentText by remember { mutableStateOf("") }
    var shapeType by remember { mutableStateOf("rectangle") }
    var isSaving by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Edit Annotations: Page ${'$'}{pageIndex + 1}", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Annotation Type:", modifier = Modifier.padding(end = 8.dp))
            DropdownMenu(
                expanded = false,
                onDismissRequest = {},
                modifier = Modifier.wrapContentSize()
            ) {
                DropdownMenuItem(onClick = { annotationType = "highlight" }, text = { Text("Highlight") })
                DropdownMenuItem(onClick = { annotationType = "comment" }, text = { Text("Comment") })
                DropdownMenuItem(onClick = { annotationType = "shape" }, text = { Text("Shape") })
            }
            Text(annotationType.capitalize(), modifier = Modifier.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("X:")
            Slider(value = x, onValueChange = { x = it }, valueRange = 0f..400f, steps = 39, modifier = Modifier.weight(1f))
            Text("Y:")
            Slider(value = y, onValueChange = { y = it }, valueRange = 0f..800f, steps = 79, modifier = Modifier.weight(1f))
        }
        if (annotationType == "highlight" || annotationType == "shape") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Width:")
                Slider(value = width, onValueChange = { width = it }, valueRange = 10f..400f, steps = 39, modifier = Modifier.weight(1f))
                Text("Height:")
                Slider(value = height, onValueChange = { height = it }, valueRange = 10f..800f, steps = 79, modifier = Modifier.weight(1f))
            }
        }
        if (annotationType == "highlight") {
            Text("Highlight Color (fixed to yellow)")
        }
        if (annotationType == "comment") {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Comment Text") },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        if (annotationType == "shape") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Shape:")
                DropdownMenu(
                    expanded = false,
                    onDismissRequest = {},
                    modifier = Modifier.wrapContentSize()
                ) {
                    DropdownMenuItem(onClick = { shapeType = "rectangle" }, text = { Text("Rectangle") })
                    DropdownMenuItem(onClick = { shapeType = "ellipse" }, text = { Text("Ellipse") })
                }
                Text(shapeType.capitalize(), modifier = Modifier.padding(start = 8.dp))
            }
            Text("Shape Color (fixed to red)")
        }
        Button(onClick = {
            isSaving = true
            loading = true
            try {
                when (annotationType) {
                    "highlight" -> addHighlightAnnotation(context, uri, pageIndex, x, y, width, height, android.graphics.Color.YELLOW)
                    "comment" -> addTextAnnotation(context, uri, pageIndex, x, y, commentText)
                    "shape" -> addShapeAnnotation(context, uri, pageIndex, x, y, width, height, shapeType, android.graphics.Color.RED)
                }
                isSaving = false
                loading = false
                onDone()
            } catch (e: Exception) {
                errorMsg = "Error adding annotation to PDF: ${'$'}{e.localizedMessage}"
                isSaving = false
                loading = false
            }
        }, enabled = !isSaving && !loading, modifier = Modifier.padding(top = 8.dp)) {
            Text(if (isSaving || loading) "Adding..." else "Add Annotation", fontSize = defaultFontSize.sp)
        }
        Button(onClick = onDone, modifier = Modifier.padding(top = 16.dp)) { Text("Done", fontSize = defaultFontSize.sp) }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
        }
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun ScanToPdfScreen(onDone: (Uri?) -> Unit) {
    val context = LocalContext.current
    var images by remember { mutableStateOf(listOf<android.graphics.Bitmap>()) }
    var isSaving by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val newBitmaps = uris.mapNotNull {
            context.contentResolver.openInputStream(it)?.use(BitmapFactory::decodeStream)
        }
        images = images + newBitmaps
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) images = images + bmp
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Scan to PDF", style = MaterialTheme.typography.titleLarge)
        Row {
            Button(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.padding(end = 8.dp)) { Text("Capture Image", fontSize = defaultFontSize.sp) }
            Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Pick from Gallery", fontSize = defaultFontSize.sp) }
        }
        if (images.isNotEmpty()) {
            Text("Selected Images:", modifier = Modifier.padding(top = 8.dp))
            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                items(images) { bmp ->
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp).padding(end = 8.dp))
                }
            }
            Button(onClick = {
                isSaving = true
                loading = true
                try {
                    val file = bitmapsToPdf(context, images)
                    isSaving = false
                    loading = false
                    onDone(Uri.fromFile(file))
                } catch (e: Exception) {
                    errorMsg = "Error creating PDF: ${'$'}{e.localizedMessage}"
                    isSaving = false
                    loading = false
                }
            }, enabled = !isSaving && !loading, modifier = Modifier.padding(top = 8.dp)) {
                Text(if (isSaving || loading) "Creating..." else "Create PDF", fontSize = defaultFontSize.sp)
            }
        }
        Button(onClick = { onDone(null) }, modifier = Modifier.padding(top = 16.dp)) { Text("Cancel", fontSize = defaultFontSize.sp) }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
        }
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun ExportShareBar(currentPdfFile: File?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(modifier = modifier.padding(8.dp)) {
        Button(onClick = {
            currentPdfFile?.let { file ->
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "application/pdf"
                val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(intent, "Share PDF"))
            }
        }, enabled = currentPdfFile != null, modifier = Modifier.padding(end = 8.dp)) {
            Text("Share PDF", fontSize = defaultFontSize.sp)
        }
        Button(onClick = {
            currentPdfFile?.let { file ->
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/pdf"
                intent.putExtra(Intent.EXTRA_TITLE, file.name)
                // This requires activity result handling for full implementation
                // For now, show a message
                Toast.makeText(context, "Use file manager to save/export PDF.", Toast.LENGTH_SHORT).show()
            }
        }, enabled = currentPdfFile != null) {
            Text("Export/Save As", fontSize = defaultFontSize.sp)
        }
    }
}

@Composable
fun PasswordProtectionBar(currentPdfFile: File?, onPasswordChanged: (File?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showSetDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    Row(modifier = modifier.padding(8.dp)) {
        Button(onClick = { showSetDialog = true }, enabled = currentPdfFile != null, modifier = Modifier.padding(end = 8.dp)) {
            Text("Set Password", fontSize = defaultFontSize.sp)
        }
        Button(onClick = { showRemoveDialog = true }, enabled = currentPdfFile != null) {
            Text("Remove Password", fontSize = defaultFontSize.sp)
        }
    }
    if (showSetDialog) {
        AlertDialog(
            onDismissRequest = { showSetDialog = false },
            title = { Text("Set PDF Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        isError = passwordError != null
                    )
                    if (passwordError != null) Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                    } else {
                        loading = true
                        try {
                            val newFile = setPdfPassword(context, Uri.fromFile(currentPdfFile), password)
                            onPasswordChanged(newFile)
                            showSetDialog = false
                            password = ""
                            passwordError = null
                        } catch (e: Exception) {
                            errorMsg = "Error setting password: ${'$'}{e.localizedMessage}"
                        } finally {
                            loading = false
                        }
                    }
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showSetDialog = false }) { Text("Cancel") }
            }
        )
    }
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove PDF Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Current Password") },
                        isError = passwordError != null
                    )
                    if (passwordError != null) Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                    } else {
                        loading = true
                        try {
                            val newFile = removePdfPassword(context, Uri.fromFile(currentPdfFile), password)
                            if (newFile != null) {
                                onPasswordChanged(newFile)
                                showRemoveDialog = false
                                password = ""
                                passwordError = null
                            } else {
                                passwordError = "Incorrect password or failed to remove."
                            }
                        } catch (e: Exception) {
                            errorMsg = "Error removing password: ${'$'}{e.localizedMessage}"
                        } finally {
                            loading = false
                        }
                    }
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
            }
        )
    }
    if (loading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
    }
    if (errorMsg != null) {
        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
    }
}

@Composable
fun SignatureBar(currentPdfFile: File?, pageIndex: Int, onSigned: (File?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    Row(modifier = modifier.padding(8.dp)) {
        Button(onClick = { showDialog = true }, enabled = currentPdfFile != null) {
            Text("Sign PDF Page", fontSize = defaultFontSize.sp)
        }
    }
    if (showDialog) {
        SignatureDialog(
            onSignature = { bmp ->
                if (currentPdfFile != null && bmp != null) {
                    loading = true
                    try {
                        val newFile = addSignatureToPdfPage(context, Uri.fromFile(currentPdfFile), pageIndex, bmp)
                        onSigned(newFile)
                    } catch (e: Exception) {
                        errorMsg = "Error adding signature: ${'$'}{e.localizedMessage}"
                    } finally {
                        loading = false
                    }
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
    if (loading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).semantics { contentDescription = "Loading indicator" })
    }
    if (errorMsg != null) {
        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = defaultFontSize.sp)
    }
}

@Composable
fun SignatureDialog(onSignature: (android.graphics.Bitmap?) -> Unit, onDismiss: () -> Unit) {
    var pathPoints by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember { mutableStateOf(listOf<Offset>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Draw Your Signature") },
        text = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(ComposeColor.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { currentPath = listOf() },
                        onDragEnd = {
                            pathPoints = pathPoints + listOf(currentPath)
                            currentPath = listOf()
                        },
                        onDragCancel = {},
                        onDrag = { change, _ ->
                            currentPath = currentPath + change.position
                        }
                    )
                }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val allPaths = pathPoints + listOf(currentPath)
                    allPaths.forEach { pathList ->
                        if (pathList.size > 1) {
                            val path = Path().apply {
                                moveTo(pathList[0].x, pathList[0].y)
                                for (pt in pathList.drop(1)) lineTo(pt.x, pt.y)
                            }
                            drawPath(path, color = ComposeColor.Black, style = Stroke(width = 4f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Convert signature to Bitmap
                val bmp = android.graphics.Bitmap.createBitmap(400, 200, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                canvas.drawColor(android.graphics.Color.WHITE)
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    strokeWidth = 4f
                    style = android.graphics.Paint.Style.STROKE
                }
                pathPoints.forEach { pathList ->
                    for (i in 1 until pathList.size) {
                        val p1 = pathList[i - 1]
                        val p2 = pathList[i]
                        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                    }
                }
                onSignature(bmp)
            }) { Text("Sign") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
