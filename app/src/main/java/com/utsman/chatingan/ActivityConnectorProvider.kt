package com.utsman.chatingan

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.utsman.chatingan.common.ActivityConnector
import java.io.File
import java.util.concurrent.Executor

class ActivityConnectorProvider(private val builder: Builder) : ActivityConnector {
    override val currentActivity: ComponentActivity
        get() = builder.currentActivity

    override val outputCameraDirectory: File
        get() = builder.outputCameraDirectory
    override val cameraExecutor: Executor
        get() = builder.cameraExecutor

    data class Builder(
        val currentActivity: ComponentActivity,
        val outputCameraDirectory: File,
        val cameraExecutor: Executor
    ) {

        fun create(): ActivityConnector {
            return ActivityConnectorProvider(this)
        }
    }
}