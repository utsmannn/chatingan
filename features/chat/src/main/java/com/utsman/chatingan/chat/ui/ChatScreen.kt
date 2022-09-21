package com.utsman.chatingan.chat.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.composeStateOf
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun ChatScreen(
    contact: Contact,
    navigationProvider: NavigationProvider = get(),
    viewModel: ChatViewModel = getViewModel()
) {
    viewModel.getChat(contact)
    Scaffold(
        topBar = {
            TopBarChat(
                contact = contact,
                onClick = {
                    navigationProvider.back()
                }
            )
        },
        bottomBar = {
            BottomBarChat(
                onSend = {
                    viewModel.sendMessage(contact.id, it)
                }
            )
        },
        content = {
            viewModel.chatState.composeStateOf {
                Column {
                    messages.forEach {
                        MessageItem(messageChat = it)
                    }
                }
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
    val context = LocalContext.current
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
                .height(60.dp),value = inputValue, onValueChange = {
                inputValue = it
            })
        }

        Button(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            onClick = {
                Toast.makeText(context, inputValue, Toast.LENGTH_SHORT).show()
                onSend.invoke(inputValue)
            },
            content = {
                Text(text = "send")
            })
    }
}

@Composable
fun MessageItem(messageChat: MessageChat) {
    Column(modifier = Modifier.wrapContentSize(align = Alignment.TopStart)) {
        Card(shape = RoundedCornerShape(6.dp), modifier = Modifier.background(Color.Magenta)) {
            Text(text = messageChat.messageBody)
        }
    }
}