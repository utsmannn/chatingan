package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent

class HomeRepositoryImpl(
    private val authDataSources: AuthDataSources
) : HomeRepository {

    private val _userState = defaultStateEvent<User>()
    override val userState: FlowEvent<User>
        get() = _userState

    override suspend fun getUser() {
        authDataSources.getCurrentUser().collect(_userState)
    }
}