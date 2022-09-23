package com.utsman.chatingan.chat.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.composeStateOf
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.doOnSuccess
import com.utsman.chatingan.common.ui.LoadingScreen
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.ext.clearQuotes

@Composable
fun ChatScreen(
    contact: Contact,
    navigationProvider: NavigationProvider = get(),
    viewModel: ChatViewModel = getViewModel()
) {
    println("ASUUUUUUUU contact nya nih-> $contact")
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val data by viewModel.chatState.collectAsState()

    viewModel.getChat(contact)
    viewModel.readChat(contact)

    Scaffold(
        topBar = {
            TopBarChat(
                contact = contact,
                onClick = {
                    navigationProvider.back()
                }
            )
        },
        content = {
            Column {
                data
                    .doOnSuccess {
                        scope.launch {
                            scrollState.scrollToItem(it.messages.size)
                        }
                        LazyColumn(
                            modifier = Modifier.weight(10f),
                            state = scrollState,
                            content = {
                                items(it.messages) {
                                    MessageItem(messageChat = it, contact = contact)
                                }
                            })
                    }.doOnLoading {
                        LoadingScreen(modifier = Modifier.weight(10f))
                    }

                BottomBarChat(
                    onSend = {
                        viewModel.sendMessage(contact.id, it)
                    }
                )
            }
        }
    )
}

@Composable
fun TopBarChat(contact: Contact, onClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = contact.image,
                    contentDescription = contact.id,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                )
                Text(text = contact.name, modifier = Modifier.padding(start = 12.dp))
            }
        },
        navigationIcon = {
            IconButton(onClick = onClick) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
            }
        }
    )
}

@Composable
fun BottomBarChat(
    onSend: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Card(
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .background(Color.White)
        ) {
            TextField(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp), value = inputValue, onValueChange = {
                inputValue = it
            })
        }

        Button(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            onClick = {
                onSend.invoke(inputValue)
                inputValue = ""
            },
            content = {
                Text(text = "send")
            })
    }
}

@Composable
fun MessageItem(messageChat: MessageChat, contact: Contact) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .safeContentPadding()
    ) {
        val containerModifier = if (messageChat.senderId == contact.id) {
            Modifier
                .align(Alignment.TopStart)
                .background(Color.Magenta, shape = RoundedCornerShape(10))
        } else {
            Modifier
                .align(Alignment.TopEnd)
                .background(Color.Gray, shape = RoundedCornerShape(10))
        }

        val messageModifier = if (messageChat.senderId == contact.id) {
            Modifier
                .widthIn(max = 250.dp)
                .wrapContentSize(align = Alignment.TopStart)
                .background(Color.Magenta, shape = RoundedCornerShape(10))
        } else {
            Modifier
                .widthIn(max = 250.dp)
                .wrapContentSize(align = Alignment.TopEnd)
                .background(Color.Gray, shape = RoundedCornerShape(10))
        }

        Column(
            modifier = containerModifier
        ) {
            Column(
                modifier = messageModifier
            ) {
                Text(text = messageChat.messageBody, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Preview
@Composable
fun PreviewChat() {
    ChatScreen(contact = Contact())
}