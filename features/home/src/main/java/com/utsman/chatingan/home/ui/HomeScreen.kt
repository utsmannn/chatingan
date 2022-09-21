package com.utsman.chatingan.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.common.event.composeStateOf
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.EmptyScreen
import com.utsman.chatingan.common.ui.FailureScreen
import com.utsman.chatingan.common.ui.LoadingScreen
import com.utsman.chatingan.home.R
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    navigationProvider: NavigationProvider = get(),
    viewModel: HomeViewModel = getViewModel()
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigationProvider.navigateToContact()
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_chat_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            )
        }
    ) {
        ColumnCenter {
            viewModel.userState.composeStateOf(
                onIdle = {
                    viewModel.getUser()
                },
                onLoading = {
                    LoadingScreen()
                },
                onSuccess = {
                    Text(text = this.toString(), color = Color.Blue)
                    Button(onClick = {
                        navigationProvider.navigateToProfile()
                    }) {
                        Text(text = "next screen")
                    }

                    viewModel.contactState.composeStateOf(
                        onLoading = {
                            LoadingScreen()
                        },
                        onFailure = {
                            FailureScreen(message = message.orEmpty())
                        },
                        onEmpty = {
                            EmptyScreen()
                        },
                        onSuccess = {
                            val idFirst = firstOrNull()?.id
                            if (!idFirst.isNullOrEmpty()) {
                                viewModel.token(idFirst)
                            }
                            Text(text = "${this.map { it.name }}")
                            viewModel.tokenState.composeStateOf {
                                Text(text = "token is-> \n$this")
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(
    navigationProvider: NavigationProvider = get()
) {
    ColumnCenter {
        Text(text = "this is profile screen", modifier = Modifier.clickable {
            navigationProvider.back()
        })
    }
}