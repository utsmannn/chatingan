package com.utsman.chatingan.login.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.utsman.chatingan.auth.authComponentBuilder
import com.utsman.chatingan.common.event.subscribeStateOf
import com.utsman.chatingan.common.launch
import com.utsman.chatingan.home.ui.HomeActivity
import com.utsman.chatingan.login.action.LoginAction
import com.utsman.chatingan.login.ui.compose.Login
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : ComponentActivity() {
    private val authComponent by authComponentBuilder(this)
    private val viewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Login(loginAction = LoginAction(
                login = {
                    viewModel.signIn(authComponent)
                }
            ))
        }

        viewModel.signInState.subscribeStateOf(
            activity = this,
            onSuccess = {
                launch(HomeActivity::class)
            }
        )
    }
}