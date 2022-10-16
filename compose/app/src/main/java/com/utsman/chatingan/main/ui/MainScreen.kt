package com.utsman.chatingan.main.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.utsman.chatingan.AppNavigationProvider
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.event.doOnFailure
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.common.ui.component.ChatinganText
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.animateComposable
import com.utsman.chatingan.contact.routes.ContactRoute
import com.utsman.chatingan.home.routes.HomeRoute
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.login.ui.LoginScreen
import com.utsman.chatingan.navigation.LocalMainProvider
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.routes.AppRoute
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatinganApp(
    navHostController: NavHostController
) {
    //val navHostController = rememberAnimatedNavController()
    //AppNavigationProvider.initialize(navHostController)

    AnimatedNavHost(
        navController = navHostController,
        startDestination = AppRoute.Main.getValue()
    ) {

        animateComposable(AppRoute.Main.getValue()) {
            MainScreen()
        }
        animateComposable(AppRoute.Splash.getValue()) {
            SplashScreen()
        }
        animateComposable(AppRoute.Login.getValue()) {
            LoginScreen()
        }

        HomeRoute.registerNavGraph(this)
        ContactRoute.registerNavGraph(this)
        ChatRoute.registerNavGraph(this)
    }
}
@Composable
fun MainScreen() {
    val navigationProvider = LocalMainProvider.current.navProvider()
    ColumnCenter {
        Button(onClick = {
            navigationProvider.navigateToSplash()
        }) {
            ChatinganText(text = "Start")
        }
    }
}

@Composable
fun SplashScreen(
    mainViewModel: MainViewModel = getViewModel()
) {
    val navigationProvider = LocalMainProvider.current.navProvider()
    val userState by mainViewModel.userState.collectAsState()
    val firebaseTokenState by mainViewModel.firebaseTokenState.collectAsState()
    val context = LocalContext.current

    userState
        .doOnLoading {
            ColumnCenter {
                ChatinganText(text = "Setting up..")
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
                val meContact = Contact.build {
                    id = user.id
                    name = user.name
                    email = user.email
                    imageUrl = user.photoUrl
                    fcmToken = token
                }

                Chatingan.updateContact(context, meContact)

                navigationProvider.screenOf(
                    routeViewModel = mainViewModel,
                    destination = HomeRoute.Home
                )
            }
        }
}