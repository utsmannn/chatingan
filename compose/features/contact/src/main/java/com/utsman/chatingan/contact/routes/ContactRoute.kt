package com.utsman.chatingan.contact.routes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.utsman.chatingan.common.ui.component.animateComposable
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

    @OptIn(ExperimentalAnimationApi::class)
    override fun registerNavGraph(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.navigation(startDestination = Contact.getValue(), route = parent) {
            animateComposable(Contact.getValue()) {
                ContactScreen()
            }
            animateComposable(AddContact.getValue()) {
                AddContactScreen()
            }
        }
    }
}