package com.utsman.chatingan.auth.datasources

import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.utsman.chatingan.auth.AuthComponent
import com.utsman.chatingan.auth.data.AuthMapper
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.koin.KoinInjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.dsl.module

class AuthDataSources {

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    internal fun getGoogleSignInClient(activity: ComponentActivity, clientId: String): GoogleSignInClient {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestId()
            .requestEmail()
            .requestProfile()
            .build()
            .run {
                GoogleSignIn.getClient(activity, this)
            }
    }

    internal fun googleSignInCallback(intent: Intent?, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit = {}) {
        GoogleSignIn.getSignedInAccountFromIntent(intent)
            .addOnSuccessListener { account ->
                val token = account.idToken
                if (token != null) {
                    val credential = GoogleAuthProvider.getCredential(token, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener {
                            val user = AuthMapper.mapFirebaseUserToUser(it.result.user)
                            onSuccess.invoke(user)
                        }
                        .addOnFailureListener {
                            it.printStackTrace()
                            onFailure.invoke(it)
                        }
                    return@addOnSuccessListener
                }
            }
    }

    suspend fun signOut(): Flow<StateEvent<Boolean>> {
        return flow {
            val signOutStateLoading = StateEvent.Loading<Boolean>()
            emit(signOutStateLoading)
            auth.signOut()

            val signOutStateSuccess = StateEvent.Success(true)
            emit(signOutStateSuccess)
        }
    }

    suspend fun getCurrentUser(): Flow<StateEvent<User>> {
        return flow {
            val userLoadingState = StateEvent.Loading<User>()
            emit(userLoadingState)

            delay(3000)
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

            emit(userFinalState)
        }
    }

    suspend fun signIn(component: AuthComponent): Flow<StateEvent<User>> {
        return flow {
            if (auth.currentUser == null) {
                component.signIn()
                component.userState.collect {
                    emit(it)
                }
            }
        }
    }

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                single { AuthDataSources() }
            }
        }
    }
}