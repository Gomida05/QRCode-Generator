package com.das.myqrcode.ui.scanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun QRScanner(onNavigateBack: () -> Unit) {

    val context = LocalContext.current

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, you can proceed with scanning
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(
                    context,
                    "The app need camara permission enable it manually in settings",
                    Toast.LENGTH_SHORT
                ).show()
                onNavigateBack()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    var qrCode by remember { mutableStateOf("") }
    var boundingBox by remember { mutableStateOf<Rect?>(null) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(LocalContext.current)
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    var isScanning by remember { mutableStateOf(false) }

    val mContext = LocalContext.current
    LaunchedEffect(cameraProviderFuture) {
        cameraProvider = cameraProviderFuture.get()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        cameraProvider?.let {
            CameraPreview(
                cameraProvider = it,
                onQRCodeDetected = { result, box ->
                    qrCode = result
                    boundingBox = box
                }

            )
        }



        if (qrCode.isNotEmpty()) {
            // Show QR code result with options
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.7f), shape = RectangleShape)
                    .padding(16.dp)
            ) {

                Text(text = "QR Code: $qrCode")
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = {
                        val clipboard =
                            mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("QR Code", qrCode)
                        clipboard.setPrimaryClip(clip)
                    }) {
                        Text("Copy")
                    }
                    Button(onClick = {

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                qrCode
                            )  // Add the text you want to share
                        }

                        // Create a chooser so that the user can choose their preferred app
                        val chooser = Intent.createChooser(shareIntent, "Share via")

                        // Start the chooser activity
                        mContext.startActivity(chooser)

                    }) {
                        Text("Share")
                    }
                }
            }
        }

        // Highlight QR bounding box with animation
        boundingBox?.let {
            HighlightQRBox(boundingBox = it, isScanning)
        }
    }

}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(cameraProvider: ProcessCameraProvider, onQRCodeDetected: (String, Rect?) -> Unit) {
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lifecycleOwner) {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder().build()
        preview.surfaceProvider = previewView.surfaceProvider

        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview
        )


        // Barcode scanning functionality
        val scanner: BarcodeScanner = BarcodeScanning.getClient()

        val imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(inputImage)

                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            val rawValue = barcode.displayValue
                            val boundingBox = barcode.boundingBox

                            if (!rawValue.isNullOrEmpty()) {

                                onQRCodeDetected(rawValue, boundingBox)
//                                barcode.geoPoint
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("QRScanner", "Barcode scanning failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }

        // Bind image analysis to lifecycle (only once here)
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
    }

    // Display preview view
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}




@Composable
fun HighlightQRBox(boundingBox: Rect, isScanning: Boolean) {


    val snapScale by animateFloatAsState(
        targetValue = if (isScanning) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .offset(x = boundingBox.left.dp, y = boundingBox.top.dp)
            .size(boundingBox.width().dp, boundingBox.height().dp)
            .graphicsLayer(
                scaleX = snapScale,
                scaleY = snapScale
            )
            .border(2.dp, Color.Green)
            .background(Color.Transparent)
    )

    // Add a scanning line moving from top to bottom
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color.Green)
    )
}
