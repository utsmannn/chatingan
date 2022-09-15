package com.utsman.chatingan.home.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.composeStateOf
import com.utsman.chatingan.common.ui.ColumnCenter
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            viewModel.userState.composeStateOf {
                Home(user = this)
            }
        }

        viewModel.getUser()
    }
}

@Composable
fun Home(user: User) {
    ColumnCenter {
        Text(text = user.toString(), color = Color.Blue)
    }
}