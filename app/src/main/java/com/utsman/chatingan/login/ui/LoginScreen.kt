package com.utsman.chatingan.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.common.R
import com.utsman.chatingan.common.event.defaultCompose
import com.utsman.chatingan.common.event.doOnIdle
import com.utsman.chatingan.common.event.doOnSuccess
import com.utsman.chatingan.common.ui.ButtonColorsWhite
import com.utsman.chatingan.common.ui.ButtonImage
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.routes.AppRoute
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(
    authComponent: AuthComponent,
    navigationProvider: NavigationProvider = get(),
    viewModel: LoginViewModel = getViewModel()
) {
    val state by viewModel.signInState.collectAsState()
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