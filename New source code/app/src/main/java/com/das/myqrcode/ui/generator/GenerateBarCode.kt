package com.das.myqrcode.ui.generator

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.IOException


@Composable
fun GenerateBarCode(onNavigateBack: ()-> Unit){
    val context = LocalContext.current
    val bitmapState: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }

    val textState = remember { mutableStateOf("") }
    val saveBarCodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                saveBarCodeToExternalStorage(it, bitmapState.value, context)
            }
            bitmapState.value = null
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = textState.value,
                onValueChange = { newText ->
                    textState.value = newText
                },
                placeholder = {
                    Text(
                        text = "Enter your content here"
                    )
                },

                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp)
                    .align(Alignment.CenterHorizontally)
//                            .background(Color(0xFF03DAC5))
                    .clip(RoundedCornerShape(28)),

                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    if (textState.value.isNotEmpty()) {
                        IconButton(onClick = { textState.value = "" }) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.Close),
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (textState.value.isNotBlank()) {
                            bitmapState.value = generateBarCode(textState.value)
                        }
                    }
                )
            )



            Spacer(modifier = Modifier.height(16.dp))

            // Generate BarCode button
            Button(
                onClick = {
                    if (textState.value.isNotBlank()) {
                        bitmapState.value = generateBarCode(textState.value)
                    } else {
                        showToast(context, "Please enter something")
                    }
                },
                modifier = Modifier
                    .width(310.dp)
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Generate", style = MaterialTheme.typography.bodyLarge)
            }

            bitmapState.value?.let { imageBitmap ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(480.dp)
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Generated BarCode",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Buttons for saving or deleting
            bitmapState.value?.let {

                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(480.dp)

                ) {


                    // Delete Button
                    Button(
                        onClick = {
                            bitmapState.value = null
                            showToast(context, "Barcode deleted")
                        },
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Delete),
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .fillMaxSize()
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            saveBarCodeLauncher.launch(null)

                        },
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Save),
                            contentDescription = "Save",
                            tint = Color.Green,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private fun generateBarCode(content: String): ImageBitmap {
    val width = 600
    val height = 300
    val writer = MultiFormatWriter()

    val hints = hashMapOf<EncodeHintType, Any>(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L
    )

    val bitMatrix: BitMatrix = writer.encode(
        content,
        BarcodeFormat.CODE_128, // Or use QR_CODE, CODE_39, etc. depending on your needs
        width,
        height,
        hints
    )

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }

    return bitmap.asImageBitmap()
}


private fun saveBarCodeToExternalStorage(uri: Uri, bitmap: ImageBitmap?, context: Context) {
    val documentFile = DocumentFile.fromTreeUri(context, uri)

    if (documentFile != null && documentFile.isDirectory) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val fileUri = documentFile.createFile("image/png", fileName)

        fileUri?.let {
            try {
                val outputStream = context.contentResolver.openOutputStream(it.uri)
                outputStream?.let { os ->
                    bitmap?.asAndroidBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, os)
                    os.close()

                    // Scan the file so it appears in the gallery
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(it.uri.path),
                        null,
                        null
                    )
                    showToast(context, "QR Code saved to gallery!")
                }
            } catch (e: IOException) {
                showToast(
                    context,
                    "Error saving QR Code: ${e.message}"
                )
            }
        } ?: run {
            showToast(context, "Failed to create file")
        }
    } else {
        showToast(context, "Selected path is not a valid directory")
    }
}

private fun showToast(context: Context, content: String){
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
}