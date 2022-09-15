package com.utsman.chatingan.home.ui.compose

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.ui.ColumnCenter

@Composable
fun Home(user: User) {
    ColumnCenter {
        Text(text = user.toString())
    }
}