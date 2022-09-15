package com.utsman.chatingan.auth.data

import com.google.firebase.auth.FirebaseUser

object AuthMapper {

    fun mapFirebaseUserToUser(firebaseUser: FirebaseUser?): User {
        return User(
            id = firebaseUser?.uid.orEmpty(),
            name = firebaseUser?.displayName.orEmpty(),
            email = firebaseUser?.email.orEmpty(),
            photoUrl = firebaseUser?.photoUrl.toString()
        )
    }
}