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
import com.utsman.chatingan.lib.utils.Utils
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.navigation.NavigationRouteModule
import com.utsman.chatingan.navigation.Route
import com.utsman.chatingan.navigation.generateDataFromKey

object ChatRoute : NavigationRouteModule {

    private const val CHAT_KEY_ARG = "chat"
    private const val CAMERA_SESSION_KEY_ARG = "contact"

    override val parent: String
        get() = "chat/main"

    object Chat : Route("$parent/chat") {
        override val arg: String
            get() = CHAT_KEY_ARG
    }

    object Camera : Route("$parent/camera") {
        override val arg: String
            get() = CAMERA_SESSION_KEY_ARG
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
    override fun registerNavGraph(navGraphBuilder: NavGraphBuilder) {
        val chatArgument = listOf(
            navArgument(CHAT_KEY_ARG) {
                type = NavType.StringType
            }
        )

        val cameraArgument = listOf(
            navArgument(CAMERA_SESSION_KEY_ARG) {
                type = NavType.StringType
            }
        )

        return navGraphBuilder.navigation(startDestination = Chat.getValue(), route = parent) {
            animateComposable(
                route = Chat.getValue(),
                arguments = chatArgument
            ) {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    val contact: Contact = it.generateDataFromKey(CHAT_KEY_ARG)
                    ChatScreen(contact = contact)
                }
            }
            animateComposable(
                route = Camera.getValue(),
                arguments = cameraArgument
            ) {
                val contact: Contact = it.generateDataFromKey(CAMERA_SESSION_KEY_ARG)
                CameraScreen(contact)
            }
        }
    }
}