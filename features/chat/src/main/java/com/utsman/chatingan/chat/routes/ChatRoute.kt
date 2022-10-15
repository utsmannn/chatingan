package com.utsman.chatingan.chat.routes

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.utsman.chatingan.chat.ui.camera.CameraScreen
import com.utsman.chatingan.chat.ui.chat.ChatScreen
import com.utsman.chatingan.common.ui.component.animateComposable
import com.utsman.chatingan.lib.Utils
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.navigation.NavigationRouteModule
import com.utsman.chatingan.navigation.Route

object ChatRoute : NavigationRouteModule {

    override val parent: String
        get() = "chat/main"

    object Chat : Route("$parent/chat") {
        override val arg: String
            get() = "contact"
    }

    object Camera : Route("$parent/camera")

    @OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
    override fun registerNavGraph(navGraphBuilder: NavGraphBuilder) {
        val argument = listOf(
            navArgument(NavigationProvider.NavArg.MESSAGE_CONTACT_ARG) {
                type = NavType.StringType
            }
        )

        return navGraphBuilder.navigation(startDestination = Chat.getValue(), route = parent) {
            animateComposable(
                route = Chat.getValue(),
                arguments = argument
            ) {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    val contactJson = it.arguments?.getString(NavigationProvider.NavArg.MESSAGE_CONTACT_ARG)
                    val contact: Contact = Utils.convertFromJson(contactJson.orEmpty())
                    ChatScreen(contact = contact)
                }
            }
            animateComposable(Camera.getValue()) {
                CameraScreen()
            }
        }
    }
}