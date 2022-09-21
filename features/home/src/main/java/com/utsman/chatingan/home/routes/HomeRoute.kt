package com.utsman.chatingan.home.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.utsman.chatingan.navigation.NavigationRouteModule
import com.utsman.chatingan.navigation.Route
import com.utsman.chatingan.home.ui.HomeScreen
import com.utsman.chatingan.home.ui.ProfileScreen

object HomeRoute : NavigationRouteModule {
    object Home : Route("$parent/main")
    object Profile : Route("$parent/profile")

    override val parent: String
        get() = "home"

    override fun registerNavGraph(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.navigation(startDestination = Home.getValue(), route = parent) {
            composable(Home.getValue()) {
                HomeScreen()
            }
            composable(Profile.getValue()) {
                ProfileScreen()
            }
        }
    }
}