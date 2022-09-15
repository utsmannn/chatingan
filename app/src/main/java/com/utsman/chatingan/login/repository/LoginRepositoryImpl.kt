package com.utsman.chatingan.login.repository

import com.utsman.chatingan.auth.AuthComponent
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginRepositoryImpl(
    private val authDataSources: AuthDataSources
) : LoginRepository {

    private val _signInState = defaultStateEvent<User>()
    override val signInState: FlowEvent<User>
        get() = _signInState

    override suspend fun signIn(authComponent: AuthComponent) {
        authDataSources.signIn(authComponent).collect(_signInState)
    }
}