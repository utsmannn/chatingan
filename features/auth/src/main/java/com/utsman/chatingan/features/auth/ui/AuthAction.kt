package com.utsman.chatingan.features.auth.ui

import com.utsman.chatingan.common.event.view.ViewState
import com.utsman.chatingan.common.event.view.ViewStateData

data class AuthAction(
    var login: ViewStateData<String> = ViewStateData { },
    var logout: ViewState = ViewState {  }
)