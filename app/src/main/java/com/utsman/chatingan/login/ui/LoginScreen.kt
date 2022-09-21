package com.utsman.chatingan.login.ui

import androidx.compose.runtime.Composable
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.common.R
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.common.event.composeStateOf
import com.utsman.chatingan.common.ui.ButtonColorsWhite
import com.utsman.chatingan.common.ui.ButtonImage
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.home.routes.HomeRoute
import com.utsman.chatingan.login.ui.LoginViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(
    authComponent: AuthComponent,
    navigationProvider: NavigationProvider = get(),
    viewModel: LoginViewModel = getViewModel()
) {
    ColumnCenter {
        viewModel.signInState.composeStateOf(
            onIdle = {
                ButtonImage(
                    resources = R.drawable.ic_google,
                    buttonColors = ButtonColorsWhite(),
                    text = "Login"
                ) {
                    viewModel.signIn(authComponent)
                }
            },
            onSuccess = {
                navigationProvider.screenOf(
                    routeViewModel = viewModel,
                    destination = HomeRoute.Home
                )
            }
        )
    }
}