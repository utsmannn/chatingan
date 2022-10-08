package com.utsman.chatingan.common

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.concurrent.Executor

interface ActivityConnector {

    val currentActivity: ComponentActivity
    val outputCameraDirectory: File
    val cameraExecutor: Executor
}