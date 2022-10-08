package com.utsman.chatingan.main.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.utsman.chatingan.ActivityConnectorProvider
import com.utsman.chatingan.R
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.auth.component.authComponentBuilder
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val authComponent: AuthComponent by authComponentBuilder(this)
    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatinganApp(authComponent = authComponent)
        }

        requestCameraPermission()
        createActivityConnector()
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

    private fun createActivityConnector() {
        val activityConnector = ActivityConnectorProvider.Builder(
            outputCameraDirectory = getOutputDirectory(),
            cameraExecutor = cameraExecutor,
            currentActivity = this
        ).create()

        module {
            single { activityConnector }
        }.run {
            loadKoinModules(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}