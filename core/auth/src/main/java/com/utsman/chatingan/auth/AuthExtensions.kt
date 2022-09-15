package com.utsman.chatingan.auth

import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.utsman.chatingan.auth.data.AuthMapper
import com.utsman.chatingan.auth.data.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KProperty

fun FirebaseAuth.getUser(): User? {
    if (currentUser == null) return null
    return AuthMapper.mapFirebaseUserToUser(currentUser)
}

@Suppress("ClassName")
class authComponentBuilder(componentActivity: ComponentActivity) : KoinComponent {

    private val authComponent = AuthComponent(componentActivity, get(), get())

    operator fun getValue(
        componentActivity: ComponentActivity,
        property: KProperty<*>
    ): AuthComponent {
        return authComponent
    }
}