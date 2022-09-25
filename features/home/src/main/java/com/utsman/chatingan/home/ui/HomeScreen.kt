package com.utsman.chatingan.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.defaultCompose
import com.utsman.chatingan.common.event.doOnEmpty
import com.utsman.chatingan.common.event.doOnFailure
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.common.ui.EmptyScreen
import com.utsman.chatingan.common.ui.FailureScreen
import com.utsman.chatingan.common.ui.LoadingScreen
import com.utsman.chatingan.common.ui.clickableRipple
import com.utsman.chatingan.common.ui.component.IconResChatDone
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.common.ui.component.IconResChatDoneAll
import com.utsman.chatingan.home.R
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.sdk.data.entity.ChatInfo
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
            chatsState.defaultCompose()
                .onSuccess { chats ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        content = {
                            items(chats) { chat ->
                                ChatScreen(
                                    contact = chat.contact,
                                    chatInfo = chat.chatInfo,
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
    chatInfo: ChatInfo,
    onClick: (Contact) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickableRipple {
                onClick.invoke(contact)
            }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            val (imageProfile, textName, textMessage, iconRead) = createRefs()
            val gh1 = createGuidelineFromStart(0.15f)

            AsyncImage(
                model = contact.image,
                contentDescription = contact.name,
                modifier = Modifier
                    .constrainAs(imageProfile) {
                        start.linkTo(parent.start)
                        end.linkTo(textName.start)
                        top.linkTo(textName.top)
                        bottom.linkTo(textMessage.bottom)
                        height = Dimension.fillToConstraints
                    }
                    .aspectRatio(1f / 1f)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(textName) {
                        start.linkTo(gh1)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        width = Dimension.fillToConstraints
                    }
            )

            val iconReadPainter = if (chatInfo.isReadFromReceiver(contact)) {
                IconResChatDoneAll()
            } else {
                IconResChatDone()
            }

            val iconReadVisibility = if (chatInfo.isFromMe(contact)) {
                Visibility.Visible
            } else {
                Visibility.Gone
            }

            Icon(
                painter = iconReadPainter,
                contentDescription = "",
                modifier = Modifier
                    .constrainAs(iconRead) {
                        start.linkTo(gh1)
                        top.linkTo(textName.bottom)
                        bottom.linkTo(textMessage.bottom)
                        height = Dimension.fillToConstraints
                        visibility = iconReadVisibility
                    }
                    .aspectRatio(1f / 1f)
                    .padding(end = 3.dp)
            )

            Text(
                text = chatInfo.lastMessage.messageBody,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (chatInfo.isNotRead(contact)) FontWeight.Bold else FontWeight.Light,
                modifier = Modifier
                    .constrainAs(textMessage) {
                        start.linkTo(iconRead.end)
                        end.linkTo(parent.end)
                        top.linkTo(textName.bottom)
                        width = Dimension.fillToConstraints
                    },
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