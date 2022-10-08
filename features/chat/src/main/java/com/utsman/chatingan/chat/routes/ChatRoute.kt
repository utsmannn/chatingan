package com.utsman.chatingan.chat.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.utsman.chatingan.chat.ui.camera.CameraScreen
import com.utsman.chatingan.chat.ui.chat.ChatScreen
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.navigation.NavigationRouteModule
import com.utsman.chatingan.navigation.Route
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.contract.JsonParcelize

object ChatRoute : NavigationRouteModule {

    override val parent: String
        get() = "chat/main"

    object Chat : Route("$parent/chat") {
        override val arg: String
            get() = "contact"
    }

    object Camera : Route("$parent/camera")


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
            composable(Camera.getValue()) {
                CameraScreen()
            }
        }
    }
}