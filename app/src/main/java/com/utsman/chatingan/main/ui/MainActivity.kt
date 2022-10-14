package com.utsman.chatingan.main.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.utsman.chatingan.ActivityCameraPropertiesProvider
import com.utsman.chatingan.R
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.auth.component.authComponentBuilder
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.navigation.ActivityCameraProperties
import com.utsman.chatingan.navigation.ActivityProvider
import com.utsman.chatingan.navigation.LocalActivityProvider
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val authComponent: AuthComponent by authComponentBuilder(this)
    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chatingan = Chatingan.getInstance()
        val cameraProperties = buildCameraProperties()

        val activityProvider = ActivityProvider().also {
            it.setChatingan(chatingan)
            it.setCameraProperties(cameraProperties)
        }

        setContent {
            CompositionLocalProvider(LocalActivityProvider provides activityProvider) {
                ChatinganApp(authComponent = authComponent)
            }
        }

        requestCameraPermission()
        createNotificationChannel()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Permission granted")
        } else {
            println("Camera not permission, finishing application")
            finish()
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                println("Camera permission granted")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                println("Show permission dialog")
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun buildCameraProperties(): ActivityCameraProperties {
        return ActivityCameraPropertiesProvider.Builder(
            outputCameraDirectory = getOutputDirectory(),
            cameraExecutor = cameraExecutor,
            currentActivity = this
        ).create()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("chatingan-anu", "ahahay", importance).apply {
                description = "descriptionText"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}