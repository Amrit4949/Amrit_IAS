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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amrit.jaago.ui.theme.Bad
import com.amrit.jaago.ui.theme.GradientButton
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.TextHi
import com.amrit.jaago.ui.theme.TextLo
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
                "Arise needs the camera to read your barcode.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextHi,
                textAlign = TextAlign.Center
            )
            GradientButton(
                text = "Allow camera",
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                from = Scan1,
                to = Scan2,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "If you refused before, open Settings, Apps, Arise, Permissions and turn the camera on.",
                fontSize = 12.5.sp,
                color = TextLo,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Corner brackets, a dimmed surround and a line that sweeps the box, so it is
 * obvious where to hold the code.
 */
@Composable
fun ScanFrame(accent: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2300), RepeatMode.Reverse)
    )
    Canvas(modifier = modifier) {
        val side = size.minDimension * 0.70f
        val l = (size.width - side) / 2f
        val t = (size.height - side) / 2f
        val dim = Color.Black.copy(alpha = 0.42f)

        // Spotlight the box by dimming the four bands around it.
        drawRect(color = dim, topLeft = Offset(0f, 0f), size = Size(size.width, t))
        drawRect(color = dim, topLeft = Offset(0f, t + side), size = Size(size.width, size.height - t - side))
        drawRect(color = dim, topLeft = Offset(0f, t), size = Size(l, side))
        drawRect(color = dim, topLeft = Offset(l + side, t), size = Size(size.width - l - side, side))

        val arm = side * 0.2f
        val w = side * 0.03f
        val corners = listOf(
            Triple(Offset(l, t), Offset(l + arm, t), Offset(l, t + arm)),
            Triple(Offset(l + side, t), Offset(l + side - arm, t), Offset(l + side, t + arm)),
            Triple(Offset(l, t + side), Offset(l + arm, t + side), Offset(l, t + side - arm)),
            Triple(Offset(l + side, t + side), Offset(l + side - arm, t + side), Offset(l + side, t + side - arm))
        )
        corners.forEach { (origin, horizontal, vertical) ->
            drawLine(accent, origin, horizontal, strokeWidth = w, cap = StrokeCap.Round)
            drawLine(accent, origin, vertical, strokeWidth = w, cap = StrokeCap.Round)
        }

        val y = t + side * sweep
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, accent.copy(alpha = 0.85f), Color.Transparent)
            ),
            start = Offset(l, y),
            end = Offset(l + side, y),
            strokeWidth = w * 0.7f,
            cap = StrokeCap.Round
        )
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

        // A torch matters here: you are scanning in a dark bedroom.
        val hasTorch = camera?.cameraInfo?.hasFlashUnit() == true
        if (hasTorch) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (torchOn) Scan1 else Color.Black.copy(alpha = 0.55f),
                        CircleShape
                    )
                    .clickable {
                        torchOn = !torchOn
                        runCatching { camera?.cameraControl?.enableTorch(torchOn) }
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = if (torchOn) "Torch on" else "Torch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (torchOn) Color(0xFF04141A) else TextHi
                )
            }
        }

        failure?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Bad,
                    textAlign = TextAlign.Center
                )
            }
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

private const val TAG = "AriseScanner"
