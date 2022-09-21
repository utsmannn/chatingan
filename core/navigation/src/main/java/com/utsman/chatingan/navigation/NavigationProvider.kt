package com.utsman.chatingan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.utsman.chatingan.sdk.data.entity.Contact

interface NavigationProvider {
    val navHostController: NavHostController

    @Composable
    fun screenOf(routeViewModel: RouteViewModel, destination: Route)

    fun back()

    /* MAIN */
    fun navigateToSplash()

    /* HOME */
    fun navigateToProfile()

    /* CHAT */
    fun navigateToChat(contact: Contact)

    /* CONTACT */
    fun navigateToContact()

    object NavArg {
        const val CONTACT_ARG = "contact"
    }
}