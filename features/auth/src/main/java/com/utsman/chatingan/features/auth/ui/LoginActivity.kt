package com.utsman.chatingan.features.auth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.utsman.chatingan.auth.authComponentBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : ComponentActivity() {
    private val viewModel: LoginViewModel by viewModel()
    private val authComponent by authComponentBuilder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Login(
                authAction = AuthAction(
                    login = {
                        viewModel.signIn(authComponent)
                    }
                )
            )
        }
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(
                Intent(context, LoginActivity::class.java)
            )
        }
    }
}