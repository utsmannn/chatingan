package com.utsman.chatingan.main.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.utsman.chatingan.AppApplication
import com.utsman.chatingan.AppNavigationProvider
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.event.doOnFailure
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.contact.routes.ContactRoute
import com.utsman.chatingan.home.routes.HomeRoute
import com.utsman.chatingan.login.ui.LoginScreen
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.routes.AppRoute
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Contact
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@Composable
fun ChatinganApp(
    authComponent: AuthComponent
) {
    val navHostController = rememberNavController()
    AppNavigationProvider.initialize(navHostController)

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
    val userState by mainViewModel.userState.collectAsState()
    val firebaseTokenState by mainViewModel.firebaseTokenState.collectAsState()

    userState
        .doOnLoading {
            ColumnCenter {
                Text(text = "Setting up..")
                CircularProgressIndicator(modifier = Modifier.padding(20.dp))
            }
        }
        .doOnFailure {
            navigationProvider.screenOf(
                routeViewModel = mainViewModel,
                destination = AppRoute.Login
            )
        }
        .onSuccess { user ->
            firebaseTokenState.onSuccess { token ->
                val contactDetail = Contact.Detail(user.email)

                val chatinganContact = ChatinganConfig.ChatinganContactBuilder()
                    .setId(user.id)
                    .setName(user.name)
                    .setImage(user.photoUrl)
                    .setDetail(contactDetail)
                    .build()

                val chatinganConfig = ChatinganConfig.ChatinganConfigBuilder()
                    .setContact(contact = chatinganContact)
                    .setServerKey(serverKey = AppApplication.SERVER_KEY)
                    .setFcmToken(fcmToken = token)
                    .build()

                println("--- SET CHATINGAN INSTANCE ---")
                Chatingan.initialize(chatinganConfig)
                navigationProvider.screenOf(
                    routeViewModel = mainViewModel,
                    destination = HomeRoute.Home
                )
            }
        }
}