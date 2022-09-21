package com.utsman.chatingan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.navigation.NavigationData
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.navigation.Route
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.contact.routes.ContactRoute
import com.utsman.chatingan.home.routes.HomeRoute
import com.utsman.chatingan.routes.AppRoute
import com.utsman.chatingan.sdk.data.entity.Contact
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class ChatinganNavigationProvider(
    override val navHostController: NavHostController
) : NavigationProvider {

    @Composable
    override fun screenOf(routeViewModel: RouteViewModel, destination: Route) {
        val navigationData = NavigationData(
            navHostController = navHostController,
            route = routeViewModel.route,
            destination = destination
        )
        LaunchedEffect(Unit) {
            navHostController.navigate(destination.getValue()) {
                popUpTo(navigationData.route.getValue()) {
                    inclusive = true
                }
            }
        }
    }

    override fun back() {
        navHostController.popBackStack()
    }

    override fun navigateToSplash() {
        navigateRoute(AppRoute.Splash)
    }

    override fun navigateToProfile() {
        navigateRoute(HomeRoute.Profile)
    }

    override fun navigateToContact() {
        navigateRoute(ContactRoute.Contact)
    }

    override fun navigateToChat(contact: Contact) {
        val jsonUri = contact.toJsonUri()
        val route = ChatRoute.Chat.getValueWithArgumentContent(jsonUri)
        navigateRoute(route)
    }

    private fun navigateRoute(route: Route) {
        navHostController.navigate(route.getValue())
    }

    companion object {

        fun initialize(navHostController: NavHostController) {
            val module = module {
                single<NavigationProvider> { ChatinganNavigationProvider(navHostController) }
            }
            loadKoinModules(module)
        }
    }
}