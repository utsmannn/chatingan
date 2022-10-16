package com.utsman.chatingan.auth.component

import androidx.activity.ComponentActivity
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.utsman.chatingan.auth.component.AuthComponent
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KProperty
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

data class AuthComponentProvider(private val authComponent: AuthComponent? = null) {
    fun authComponent(): AuthComponent = checkNotNull(authComponent)
}

val LocalAuthComponentProvider = compositionLocalOf { AuthComponentProvider() }