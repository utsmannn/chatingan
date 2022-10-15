package com.utsman.chatingan.navigation

import androidx.activity.ComponentActivity
import java.io.File
import java.util.concurrent.Executor

interface ActivityCameraProperties {

    val currentActivity: ComponentActivity
    val outputCameraDirectory: File
    val cameraExecutor: Executor
}