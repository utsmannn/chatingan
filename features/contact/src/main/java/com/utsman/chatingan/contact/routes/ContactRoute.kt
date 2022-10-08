package com.utsman.chatingan.contact.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.utsman.chatingan.contact.ui.AddContactScreen
import com.utsman.chatingan.navigation.NavigationRouteModule
import com.utsman.chatingan.navigation.Route
import com.utsman.chatingan.contact.ui.ContactScreen
import com.utsman.chatingan.navigation.NavigationProvider

object ContactRoute : NavigationRouteModule {

    object Contact : Route("$parent/main") {
        override val arg: String
            get() = "contact"
    }

    object AddContact : Route("$parent/add_contact") {
        override val arg: String
            get() = "add_contact"
    }

    override val parent: String
        get() = "contact"

    override fun registerNavGraph(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.navigation(startDestination = Contact.getValue(), route = parent) {
            composable(Contact.getValue()) {
                ContactScreen()
            }
            composable(AddContact.getValue()) {
                AddContactScreen()
            }
        }
    }
}