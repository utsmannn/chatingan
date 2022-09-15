package com.utsman.chatingan.login.ui.compose

import androidx.compose.runtime.Composable
import com.utsman.chatingan.common.ui.ButtonColorsWhite
import com.utsman.chatingan.common.ui.ButtonImage
import com.utsman.chatingan.common.ui.ColumnCenter
import com.utsman.chatingan.login.action.LoginAction

@Composable
fun Login(loginAction: LoginAction) {
    ColumnCenter {
        ButtonImage(
            resources = com.utsman.chatingan.common.R.drawable.ic_google,
            buttonColors = ButtonColorsWhite(),
            text = "Login"
        ) {
            loginAction.login.run()
        }
    }
}