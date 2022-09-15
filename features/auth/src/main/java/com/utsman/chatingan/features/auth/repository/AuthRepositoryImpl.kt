package com.utsman.chatingan.features.auth.repository

import com.utsman.chatingan.auth.AuthComponent
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.common.nothing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val authDataSources: AuthDataSources
) : AuthRepository {

    private val _isHasSignIn = MutableStateFlow(false)
    override val isHasSignIn: StateFlow<Boolean>
        get() = _isHasSignIn

    private val _signInState = defaultStateEvent<User>()
    override val signInState: StateFlow<StateEvent<User>>
        get() = _signInState

    private val _signOutState = defaultStateEvent<Nothing>()
    override val signOutState: StateFlow<StateEvent<Nothing>>
        get() = _signOutState

    override suspend fun signIn(component: AuthComponent) {
        authDataSources.signIn(component).collect {
            _signInState.value = it
            _isHasSignIn.value = it is StateEvent.Success
        }
    }

    override suspend fun signOut() {
        authDataSources.signOut()
        val emptyState = StateEvent.Empty<User>()
        _signOutState.value = emptyState.map { nothing() }
        _isHasSignIn.value = false
    }
}