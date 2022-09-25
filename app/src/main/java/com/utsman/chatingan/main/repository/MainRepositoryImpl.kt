package com.utsman.chatingan.main.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.invoke
import com.utsman.chatingan.common.event.loadingEventValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy

class MainRepositoryImpl(
    private val authDataSources: AuthDataSources
) : MainRepository {

    private val _user = defaultStateEvent<User>()
    override val userState: FlowEvent<User>
        get() = _user

    private val _firebaseTokenState = defaultStateEvent<String>()
    override val firebaseTokenState: FlowEvent<String>
        get() = _firebaseTokenState

    override suspend fun checkUser() {
        _user.value = loadingEventValue()
        authDataSources.getCurrentUser()
            .distinctUntilChangedBy { it.invoke() }
            .collect(_user)
    }

    override suspend fun getFirebaseToken() {
        _firebaseTokenState.value = loadingEventValue()
        authDataSources.firebaseToken {
            if (_firebaseTokenState.value.invoke() != it) {
                _firebaseTokenState.value = StateEvent.Success(it)
            }
        }
    }
}