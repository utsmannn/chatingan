package com.utsman.chatingan.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.auth.component.authComponentBuilder

class MainActivity : ComponentActivity() {
    private val authComponent: AuthComponent by authComponentBuilder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatinganApp(authComponent = authComponent)
        }
    }
}