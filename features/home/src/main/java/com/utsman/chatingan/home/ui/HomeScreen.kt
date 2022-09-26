package com.utsman.chatingan.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.defaultCompose
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.common.ui.clickableRipple
import com.utsman.chatingan.common.ui.component.IconResChatDone
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.common.ui.component.IconResChatDoneAll
import com.utsman.chatingan.home.R
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.utils.DateUtils
import com.utsman.chatingan.sdk.utils.isAllRead
import com.utsman.chatingan.sdk.utils.isFromMe
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
    val contactMe = Chatingan.getInstance().config.contact
    val lastMessage = chatInfo.lastMessage
    val isHasRead = lastMessage.isAllRead()
    val isFromMe = lastMessage.isFromMe(Chatingan.getInstance().config)

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
                .padding(vertical = 6.dp, horizontal = 12.dp)
        ) {
            val (
                imageProfile, textName, textMessage,
                iconRead, unreadCount, textDate
            ) = createRefs()

            val gv1 = createGuidelineFromStart(0.1f)
            val gv2 = createGuidelineFromEnd(0.1f)

            AsyncImage(
                model = contact.image,
                contentDescription = contact.name,
                modifier = Modifier
                    .constrainAs(imageProfile) {
                        start.linkTo(parent.start)
                        end.linkTo(textName.start, margin = 12.dp)
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
                        start.linkTo(gv1, margin = 12.dp)
                        end.linkTo(gv2)
                        top.linkTo(parent.top)
                        width = Dimension.fillToConstraints
                    },
                maxLines = 1
            )

            val iconReadPainter = if (isHasRead) {
                IconResChatDoneAll()
            } else {
                IconResChatDone()
            }

            val iconReadVisibility = if (isFromMe) {
                Visibility.Visible
            } else {
                Visibility.Gone
            }

            Icon(
                painter = iconReadPainter,
                contentDescription = "",
                modifier = Modifier
                    .constrainAs(iconRead) {
                        start.linkTo(gv1, margin = 12.dp)
                        top.linkTo(textName.bottom)
                        bottom.linkTo(textMessage.bottom)
                        height = Dimension.fillToConstraints
                        visibility = iconReadVisibility
                    }
                    .aspectRatio(1f / 1f)
            )

            val fontWeight = if (isHasRead || isFromMe) {
                FontWeight.Light
            } else {
                FontWeight.Bold
            }

            Text(
                text = chatInfo.lastMessage.messageBody,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = fontWeight,
                modifier = Modifier
                    .constrainAs(textMessage) {
                        start.linkTo(iconRead.end)
                        end.linkTo(textName.end)
                        top.linkTo(textName.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .padding(horizontal = 12.dp)
            )

            val unreadCountVisibility = if (chatInfo.unread >= 1 && !isFromMe) {
                Visibility.Visible
            } else {
                Visibility.Gone
            }

            UnreadCount(
                modifier = Modifier
                    .constrainAs(unreadCount) {
                        start.linkTo(gv2, margin = 6.dp)
                        end.linkTo(parent.end, margin = 6.dp)
                        top.linkTo(textName.bottom, margin = 2.dp)
                        bottom.linkTo(textMessage.bottom, margin = 2.dp)
                        visibility = unreadCountVisibility
                        width = Dimension.fillToConstraints
                    }
                    .padding(horizontal = 3.dp)
                    .background(Color.Red, shape = RoundedCornerShape(80)),
                count = chatInfo.unread
            )

            Text(
                text = DateUtils.generateDateChat(chatInfo.lastMessage.lastUpdate),
                fontSize = 10.sp,
                maxLines = 1,
                modifier = Modifier
                    .constrainAs(textDate) {
                        end.linkTo(parent.end, margin = 6.dp)
                        start.linkTo(gv2, margin = 6.dp)
                        top.linkTo(textName.top)
                        bottom.linkTo(textName.bottom)
                    }
            )
        }
    }
}

@Composable
fun UnreadCount(modifier: Modifier, count: Int) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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