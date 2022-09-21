package com.utsman.chatingan.navigation

import androidx.navigation.NavGraphBuilder

interface NavigationRouteModule {
    val parent: String
    fun registerNavGraph(navGraphBuilder: NavGraphBuilder)
}