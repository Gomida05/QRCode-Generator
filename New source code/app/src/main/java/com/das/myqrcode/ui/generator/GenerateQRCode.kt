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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.documentfile.provider.DocumentFile
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.IOException
import kotlin.Any


@Composable
fun GenerateQRCode(onNavigateBack: ()-> Unit = {}, textValue: String = "") {

    val textState = remember { mutableStateOf(textValue) }
    val context = LocalContext.current
    val bitmapState = remember { mutableStateOf<ImageBitmap?>(null) }

    val saveQrCodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                saveQRCodeToExternalStorage(it, bitmapState.value, context)
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

                leadingIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, "navigateUp")
                    }
                },


                trailingIcon = {
                    if (textState.value.isNotEmpty()) {
                        IconButton(onClick = { textState.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
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
                            bitmapState.value = generateQRCode(textState.value)
                        }
                    }
                )
            )



            Spacer(modifier = Modifier.height(16.dp))

            // Generate QR Code button
            Button(
                onClick = {
                    if (textState.value.isNotBlank()) {
                        bitmapState.value = generateQRCode(textState.value)
                    } else {
                        showToast(context, "Please enter something")
                    }
                },
                modifier = Modifier
                    .width(310.dp)
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .height(56.dp),
                shape = RoundedCornerShape(15)
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Generate",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Icon(
                        imageVector = Icons.Default.Draw,
                        "",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                    )
                }
            }

            bitmapState.value?.let { imageBitmap ->
                Box(
                    modifier = Modifier.fillMaxWidth().height(480.dp)
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Generated QR Code",
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
                            showToast(context, "QR code deleted")
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
                            saveQrCodeLauncher.launch(null)

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

//@Preview
@Composable
private fun CustomQRCode(){
    Dialog(
        onDismissRequest = { }
    ) {
        Column {
            Text("")

            TextButton(
                onClick = { }
            ) {
                Text("Select colours")
            }

        }
    }
}

private fun showToast(context: Context, content: String){
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
}


private fun generateQRCode(content: String): ImageBitmap {
    val writer = MultiFormatWriter()

    val hints = hashMapOf<EncodeHintType, Any>(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L,
    )


    val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)

    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            pixels[y * width + x] = if (bitMatrix.get(
                    x,
                    y
                )
            ) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

    return bitmap.asImageBitmap()
}




private fun saveQRCodeToExternalStorage(uri: Uri, bitmap: ImageBitmap?, context: Context) {
    val documentFile = DocumentFile.fromTreeUri(context, uri)

    if (documentFile != null && documentFile.isDirectory) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val fileUri = documentFile.createFile("image/png", fileName)

        fileUri?.let {
            try {
                val outputStream = context.contentResolver.openOutputStream(it.uri)
                outputStream?.let { os ->
                    bitmap?.asAndroidBitmap()?.compress(Bitmap.CompressFormat.JPEG, 100, os)
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
