package com.utsman.chatingan.common.ui.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable


@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animateComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = { enterTransition() },
    exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = { exitTransition() },
    popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = { popEnterTransition() },
    popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = { popExitTransition() },
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route,
    arguments,
    deepLinks,
    enterTransition,
    exitTransition,
    popEnterTransition,
    popExitTransition,
    content
)

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentScope<NavBackStackEntry>.enterTransition() =
    slideIntoContainer(
        AnimatedContentScope.SlideDirection.Left, animationSpec = tween(
            DURATION_ANIMATION_TRANSITION
        )
    )

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentScope<NavBackStackEntry>.exitTransition() =
    slideOutOfContainer(
        AnimatedContentScope.SlideDirection.Left, animationSpec = tween(
            DURATION_ANIMATION_TRANSITION
        )
    )

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentScope<NavBackStackEntry>.popEnterTransition() =
    slideIntoContainer(
        AnimatedContentScope.SlideDirection.Right, animationSpec = tween(
            DURATION_ANIMATION_TRANSITION
        )
    )

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentScope<NavBackStackEntry>.popExitTransition() =
    slideOutOfContainer(
        AnimatedContentScope.SlideDirection.Right, animationSpec = tween(
            DURATION_ANIMATION_TRANSITION
        )
    )

const val DURATION_ANIMATION_TRANSITION = 600