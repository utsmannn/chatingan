package com.utsman.chatingan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.MessageInfo

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
    fun navigateToCamera(contact: Contact)

    /* CONTACT */
    fun navigateToContact()
    fun navigateToAddContact()
}

