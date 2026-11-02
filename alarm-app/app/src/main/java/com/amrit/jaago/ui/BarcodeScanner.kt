package com.amrit.jaago.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/** Shows [content] once the camera permission is granted, and asks for it if not. */
@Composable
fun WithCameraPermission(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var asked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result -> granted = result }

    LaunchedEffect(Unit) {
        if (!granted && !asked) {
            asked = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (granted) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Jaago needs the camera to read your barcode.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("Allow camera")
            }
            Text(
                "If you refused it before, open Settings, Apps, Jaago, Permissions and turn the camera on.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Live camera preview that reports every barcode or QR code it sees.
 * [onBarcode] can fire many times a second, so the caller decides what to do with it.
 */
@Composable
fun BarcodeScannerView(
    modifier: Modifier = Modifier,
    onBarcode: (value: String, format: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val callback by rememberUpdatedState(onBarcode)

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            runCatching { scanner.close() }
            executor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val provider = withContext(Dispatchers.IO) {
                ProcessCameraProvider.getInstance(context).get()
            }
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { imageProxy ->
                val media = imageProxy.image
                if (media == null) {
                    imageProxy.close()
                } else {
                    val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(input)
                        .addOnSuccessListener { codes ->
                            val hit = codes.firstOrNull { !it.rawValue.isNullOrBlank() }
                            if (hit != null) {
                                callback(hit.rawValue.orEmpty(), formatName(hit.format))
                            }
                        }
                        .addOnFailureListener { t -> Log.w(TAG, "Scan failed", t) }
                        .addOnCompleteListener { imageProxy.close() }
                }
            }
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Could not open the camera", t)
            failure = "The camera could not be opened."
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        val hasTorch = camera?.cameraInfo?.hasFlashUnit() == true
        if (hasTorch) {
            TextButton(
                onClick = {
                    torchOn = !torchOn
                    runCatching { camera?.cameraControl?.enableTorch(torchOn) }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(if (torchOn) "Torch off" else "Torch on")
            }
        }
        failure?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatName(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR code"
    Barcode.FORMAT_EAN_13 -> "EAN 13"
    Barcode.FORMAT_EAN_8 -> "EAN 8"
    Barcode.FORMAT_UPC_A -> "UPC A"
    Barcode.FORMAT_UPC_E -> "UPC E"
    Barcode.FORMAT_CODE_128 -> "Code 128"
    Barcode.FORMAT_CODE_39 -> "Code 39"
    Barcode.FORMAT_CODE_93 -> "Code 93"
    Barcode.FORMAT_CODABAR -> "Codabar"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_AZTEC -> "Aztec"
    else -> "Barcode"
}

private const val TAG = "JaagoScanner"
