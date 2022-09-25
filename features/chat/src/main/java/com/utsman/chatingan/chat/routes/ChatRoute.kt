package com.utsman.chatingan.chat.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.utsman.chatingan.chat.ui.ChatScreen
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.navigation.NavigationRouteModule
import com.utsman.chatingan.navigation.Route
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.contract.JsonParcelize

object ChatRoute : NavigationRouteModule {
    //val ChatArg = Chat(NavigationProvider.NavArg.CONTACT_ARG)

    override val parent: String
        get() = "chat/main"

    /*class Chat(private val arg: String) : Route {
        override val value: String
            get() = "$parent/{$arg}"
    }*/

    object Chat : Route("$parent/chat") {
        override val arg: String
            get() = "contact"
    }


    override fun registerNavGraph(navGraphBuilder: NavGraphBuilder) {
        val argument = listOf(
            navArgument(NavigationProvider.NavArg.CONTACT_ARG) {
                type = NavType.StringType
            }
        )

        return navGraphBuilder.navigation(startDestination = Chat.getValue(), route = parent) {
            composable(Chat.getValue(), arguments = argument) {
                val jsonContact = it.arguments?.getString(NavigationProvider.NavArg.CONTACT_ARG)
                val contact = JsonParcelize.toObjectUri(jsonContact) ?: Contact()
                ChatScreen(contact)
            }
        }
    }
}