package com.utsman.chatingan.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.utsman.chatingan.common.event.subscribeStateOf
import com.utsman.chatingan.common.launch
import com.utsman.chatingan.common.toast
import com.utsman.chatingan.common.ui.ChatinganTheme
import com.utsman.chatingan.common.ui.Loading
import com.utsman.chatingan.home.ui.HomeActivity
import com.utsman.chatingan.login.ui.LoginActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Loading()
        }

        viewModel.checkUser()
        viewModel.userState.subscribeStateOf(
            this,
            onEmpty = {
                launch(LoginActivity::class)
                finish()
            },
            onFailure = {
                launch(LoginActivity::class)
                finish()
            },
            onSuccess = {
                toast("ada nih ...")
                launch(HomeActivity::class)
                finish()
            }
        )
    }
}

@Composable
fun Greeting(name: String, click: () -> Unit) {
    Text(text = "Hello $name!", modifier = Modifier.clickable {
        click.invoke()
    })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatinganTheme {
        Greeting("Android") {}
    }
}