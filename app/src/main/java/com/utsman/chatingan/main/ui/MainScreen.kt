package com.utsman.chatingan.main.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.utsman.chatingan.ChatinganNavigationProvider
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.common.event.composeStateOf
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.contact.routes.ContactRoute
import com.utsman.chatingan.home.routes.HomeRoute
import com.utsman.chatingan.login.ui.LoginScreen
import com.utsman.chatingan.routes.AppRoute
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@Composable
fun ChatinganApp(
    authComponent: AuthComponent
) {
    val navHostController = rememberNavController()
    ChatinganNavigationProvider.initialize(navHostController)

    NavHost(navController = navHostController, startDestination = AppRoute.Main.getValue()) {
        composable(AppRoute.Main.getValue()) {
            MainScreen()
        }
        composable(AppRoute.Splash.getValue()) {
            SplashScreen()
        }
        composable(AppRoute.Login.getValue()) {
            LoginScreen(authComponent = authComponent)
        }

        HomeRoute.registerNavGraph(this)
        ContactRoute.registerNavGraph(this)
        ChatRoute.registerNavGraph(this)
    }
}

@Composable
fun MainScreen(navigationProvider: NavigationProvider = get()) {
    ColumnCenter {
        Button(onClick = {
            navigationProvider.navigateToSplash()
        }) {
            Text(text = "Start")
        }
    }
}

@Composable
fun SplashScreen(
    navigationProvider: NavigationProvider = get(),
    mainViewModel: MainViewModel = getViewModel()
) {
    mainViewModel.userState.composeStateOf(
        onFailure = {
            navigationProvider.screenOf(
                routeViewModel = mainViewModel,
                destination = AppRoute.Login
            )
        },
        onSuccess = {
            navigationProvider.screenOf(
                routeViewModel = mainViewModel,
                destination = HomeRoute.Home
            )
        }
    )
}