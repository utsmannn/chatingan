package com.utsman.chatingan.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.auth.component.LocalAuthComponentProvider
import com.utsman.chatingan.common.R
import com.utsman.chatingan.common.event.defaultCompose
import com.utsman.chatingan.common.event.doOnIdle
import com.utsman.chatingan.common.event.doOnSuccess
import com.utsman.chatingan.common.ui.component.ButtonColorsWhite
import com.utsman.chatingan.common.ui.component.ButtonImage
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.navigation.LocalMainProvider
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.routes.AppRoute
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = getViewModel()
) {
    val state by viewModel.signInState.collectAsState()
    val navigationProvider = LocalMainProvider.current.navProvider()
    val authComponent = LocalAuthComponentProvider.current.authComponent()

    ColumnCenter {
        state.defaultCompose()
            .doOnIdle {
                ButtonImage(
                    resources = R.drawable.ic_google,
                    buttonColors = ButtonColorsWhite(),
                    text = "Login"
                ) {
                    viewModel.signIn(authComponent)
                }
            }
            .doOnSuccess {
                navigationProvider.screenOf(
                    routeViewModel = viewModel,
                    destination = AppRoute.Splash
                )
            }
    }
}