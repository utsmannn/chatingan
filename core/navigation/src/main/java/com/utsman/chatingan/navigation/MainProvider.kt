package com.utsman.chatingan.navigation

import androidx.compose.runtime.compositionLocalOf
import com.utsman.chatingan.lib.Chatingan

class MainProvider {

    private var _chatingan: Chatingan? = null
    private var _cameraProperties: ActivityCameraProperties? = null
    private var _navigationProvider: NavigationProvider? = null

    fun chatingan(): Chatingan {
        return checkNotNull(_chatingan)
    }

    fun activityCameraProperties(): ActivityCameraProperties {
        return checkNotNull(_cameraProperties)
    }

    fun navProvider(): NavigationProvider {
        return checkNotNull(_navigationProvider)
    }

    fun setChatingan(chatingan: Chatingan) {
        _chatingan = chatingan
    }

    fun setCameraProperties(cameraProperties: ActivityCameraProperties) {
        _cameraProperties = cameraProperties
    }

    fun setNavigationProvider(navigationProvider: NavigationProvider) {
        _navigationProvider = navigationProvider
    }
}

val LocalMainProvider = compositionLocalOf { MainProvider() }