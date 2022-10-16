package com.utsman.chatingan.auth.component

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.utsman.chatingan.auth.data.AuthConfig
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.MutableFlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AuthComponent(
    private val componentActivity: ComponentActivity,
    private val authConfig: AuthConfig,
    private val authDataSources: AuthDataSources
) {

    private val _userState: MutableFlowEvent<User> = MutableStateFlow(StateEvent.Loading())
    val userState: FlowEvent<User>
        get() = _userState

    private val resultContent = componentActivity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val data = it.data
        IOScope().launch {
            val loadingState = StateEvent.Loading<User>()
            _userState.emit(loadingState)
            authDataSources.googleSignInCallback(
                intent = data,
                onSuccess = { user ->
                    val successState = StateEvent.Success(user)
                    _userState.value = successState
                },
                onFailure = { exception ->
                    val failureState = StateEvent.Failure<User>(exception)
                    _userState.value = failureState
                }
            )
        }
    }

    fun signIn() {
        val clientIntent = authDataSources
            .getGoogleSignInClient(componentActivity, authConfig.clientId)
            .signInIntent
        resultContent.launch(clientIntent)
    }
}