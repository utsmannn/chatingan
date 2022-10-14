package com.utsman.chatingan

import androidx.activity.ComponentActivity
import com.utsman.chatingan.navigation.ActivityCameraProperties
import java.io.File
import java.util.concurrent.Executor

class ActivityCameraPropertiesProvider(private val builder: Builder) : ActivityCameraProperties {
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

        fun create(): ActivityCameraProperties {
            return ActivityCameraPropertiesProvider(this)
        }
    }
}