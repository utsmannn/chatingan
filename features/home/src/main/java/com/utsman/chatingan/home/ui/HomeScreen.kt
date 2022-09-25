package com.utsman.chatingan.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.doOnEmpty
import com.utsman.chatingan.common.event.doOnFailure
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.common.ui.EmptyScreen
import com.utsman.chatingan.common.ui.FailureScreen
import com.utsman.chatingan.common.ui.LoadingScreen
import com.utsman.chatingan.common.ui.clickableRipple
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.home.R
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.sdk.data.entity.Contact
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    navigationProvider: NavigationProvider = get(),
    viewModel: HomeViewModel = getViewModel()
) {
    val chatsState by viewModel.chatState.collectAsState()

    viewModel.getUser()
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
        DefaultLayoutAppBar(title = "Chatingan") {

            chatsState.doOnLoading {
                LoadingScreen()
            }
            chatsState.doOnEmpty {
                EmptyScreen()
            }
            chatsState.doOnFailure {
                FailureScreen(message = it.message.orEmpty())
            }
            chatsState.onSuccess { chats ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        items(chats) { chat ->
                            val message = chat.chatInfo.lastMessage
                            ChatScreen(
                                contact = chat.contact,
                                message = message.messageBody,
                                onClick = {
                                    navigationProvider.navigateToChat(chat.contact)
                                }
                            )
                        }
                    })
            }
        }
    }
}

@Composable
fun ChatScreen(
    contact: Contact,
    message: String,
    onClick: (Contact) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .width(80.dp)
            .clickableRipple {
                onClick.invoke(contact)
            }
    ) {
        ColumnCenter(
            modifier = Modifier
                .size(80.dp)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = contact.image,
                contentDescription = contact.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
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