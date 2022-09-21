package com.utsman.chatingan.navigation

import androidx.navigation.NavHostController

data class NavigationData(
    var navHostController: NavHostController,
    var route: Route,
    var destination: Route = Route.empty
)