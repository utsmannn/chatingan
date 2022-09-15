package com.utsman.chatingan.main.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent

class MainRepositoryImpl(
    private val authDataSources: AuthDataSources
) : MainRepository {

    private val _user = defaultStateEvent<User>()
    override val userState: FlowEvent<User>
        get() = _user

    override suspend fun checkUser() {
        authDataSources.getCurrentUser().collect(_user)
    }
}