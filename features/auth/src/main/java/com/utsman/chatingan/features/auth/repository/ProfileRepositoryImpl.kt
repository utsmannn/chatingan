package com.utsman.chatingan.features.auth.repository

import com.google.firebase.auth.FirebaseAuth
import com.utsman.chatingan.auth.data.AuthMapper
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ProfileRepositoryImpl : ProfileRepository {

    private val _userState = defaultStateEvent<User>()
    override val userState: FlowEvent<User>
        get() = _userState

    override fun initializeUser(scope: CoroutineScope) {
        scope.launch {
            val userLoadingState = StateEvent.Loading<User>()
            _userState.value = userLoadingState

            val userFinalState = FirebaseAuth.getInstance()
                .currentUser
                .let {
                    if (it != null) {
                        val user = AuthMapper.mapFirebaseUserToUser(it)
                        StateEvent.Success(user)
                    } else {
                        val throwable = Throwable("User not logged in")
                        StateEvent.Failure(throwable)
                    }
                }

            _userState.value = userFinalState
        }
    }
}