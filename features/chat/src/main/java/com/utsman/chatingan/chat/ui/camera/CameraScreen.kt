package com.utsman.chatingan.chat.ui.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.utsman.chatingan.chat.routes.BackPassChat
import com.utsman.chatingan.navigation.ActivityCameraProperties
import com.utsman.chatingan.navigation.LocalActivityProvider
import com.utsman.chatingan.navigation.NavigationProvider
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume

@Composable
fun CameraScreen(
    navigationProvider: NavigationProvider = get(),
    viewModel: CameraViewModel = getViewModel(),
    backPassChat: BackPassChat = get()
) {

    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val activityCameraProperties = LocalActivityProvider.current.activityCameraProperties()

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val imageResult by viewModel.imageFileState.collectAsState()

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        if (imageResult.isSuccess) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = imageResult.getOrNull(),
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        } else {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        }

        IconButton(
            modifier = Modifier.padding(bottom = 20.dp),
            onClick = {
                if (imageResult.isSuccess) {
                    backPassChat.setBackFrom("CAMERA_VIEW")
                    navigationProvider.back()
                } else {
                    takePhoto(
                        filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                        imageCapture = imageCapture,
                        outputDirectory = activityCameraProperties.outputCameraDirectory,
                        executor = activityCameraProperties.cameraExecutor,
                        onImageCaptured = {
                            coroutineScope.launch {
                                val compressedFile = compressedPhoto(context, it)
                                viewModel.sendFile(compressedFile)
                            }
                        },
                        onError = {
                            it.printStackTrace()
                        }
                    )
                }
            },
            content = {
                val iconImage = if (imageResult.isSuccess) {
                    Icons.Sharp.CheckCircle
                } else {
                    Icons.Sharp.Lens
                }
                Icon(
                    imageVector = iconImage,
                    contentDescription = "Take picture",
                    tint = Color.White,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(1.dp)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        )
    }
}

/**
 * Source: https://www.kiloloco.com/articles/015-camera-jetpack-compose/
 * */
private fun takePhoto(
    filenameFormat: String,
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (File) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    val photoFile = File(
        outputDirectory,
        SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            onImageCaptured(photoFile)
        }
    })
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { task ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                task.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private suspend fun compressedPhoto(context: Context, file: File): File {
    val destinationFile = File(file.parent, file.nameWithoutExtension + "-compressed." + file.extension)
    val compressedFile = Compressor.compress(context, file) {
        default(quality = 70)
        destination(destinationFile)
    }
    file.delete()
    return compressedFile
}