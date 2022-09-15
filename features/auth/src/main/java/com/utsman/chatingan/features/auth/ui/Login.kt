package com.utsman.chatingan.features.auth.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.utsman.chatingan.common.ui.ButtonImage
import com.utsman.chatingan.common.ui.ChatinganTheme
import com.utsman.chatingan.common.ui.ColumnCenter

@Composable
fun Login(
    authAction: AuthAction
) {
    ChatinganTheme {
        ColumnCenter {
            ButtonImage(
                resources = com.utsman.chatingan.common.R.drawable.ic_google,
                content = {
                    Text("Sign in", color = Color.White)
                },
                onClick = {
                    authAction.login.run("anu mas")
                }
            )
            Button(onClick = {
                authAction.logout.run()
            }) {
                Text(text = "back")
            }
        }
    }
}