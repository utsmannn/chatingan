package com.utsman.chatingan.navigation

import androidx.compose.runtime.compositionLocalOf
import com.utsman.chatingan.lib.Chatingan

class ActivityProvider {

    private var _chatingan: Chatingan? = null
    private var _cameraProperties: ActivityCameraProperties? = null

    fun chatingan(): Chatingan {
        return checkNotNull(_chatingan)
    }

    fun activityCameraProperties(): ActivityCameraProperties {
        return checkNotNull(_cameraProperties)
    }

    fun setChatingan(chatingan: Chatingan) {
        _chatingan = chatingan
    }

    fun setCameraProperties(cameraProperties: ActivityCameraProperties) {
        _cameraProperties = cameraProperties
    }
}

val LocalActivityProvider = compositionLocalOf { ActivityProvider() }