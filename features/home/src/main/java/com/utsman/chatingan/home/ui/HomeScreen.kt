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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.common.ui.clickableRipple
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.common.ui.component.IconResChatDone
import com.utsman.chatingan.common.ui.component.IconResChatDoneAll
import com.utsman.chatingan.common.ui.component.IconResChatDoneAllRead
import com.utsman.chatingan.common.ui.component.IconResChatFailure
import com.utsman.chatingan.home.R
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.ellipsize
import com.utsman.chatingan.lib.ifTextMessage
import com.utsman.chatingan.navigation.LocalMainProvider
import com.utsman.chatingan.navigation.NavigationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = getViewModel()
) {
    val navigationProvider = LocalMainProvider.current.navProvider()

    val chatsState by viewModel.chatState.collectAsState()
    val meContact = remember {
        Chatingan.getInstance().getConfiguration().contact
    }

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
            Text(text = meContact.id)
            chatsState.defaultCompose()
                .onSuccess { chats ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        content = {
                            items(chats) { messageInfo ->
                                ChatItemScreen(
                                    messageInfo = messageInfo,
                                    meContact = meContact,
                                    onClick = {
                                        navigationProvider.navigateToChat(it)
                                    }
                                )
                            }
                        })
                }
        }
    }
}

@Composable
fun ChatItemScreen(
    messageInfo: MessageInfo,
    meContact: Contact,
    onClick: (Contact) -> Unit
) {
    val lastMessage = messageInfo.lastMessage
    val isFromMe = lastMessage.getChildSenderId() == meContact.id

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickableRipple {
                onClick.invoke(messageInfo.receiver)
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

            AsyncImage(
                model = messageInfo.receiver.imageUrl,
                contentDescription = messageInfo.receiver.name,
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
                text = messageInfo.receiver.name,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .constrainAs(textName) {
                        start.linkTo(gv1, margin = 12.dp)
                        end.linkTo(textDate.start, margin = 6.dp)
                        top.linkTo(parent.top)
                        width = Dimension.fillToConstraints
                    },
                maxLines = 1
            )

            val iconReadPainter = when (lastMessage.getChildStatus()) {
                Message.Status.RECEIVED, Message.Status.READ -> {
                    IconResChatDoneAll()
                }
                Message.Status.FAILURE -> {
                    IconResChatFailure()
                }
                else -> {
                    IconResChatDone()
                }
            }

            val iconTint = when (lastMessage.getChildStatus()) {
                Message.Status.READ -> {
                    Color.Blue
                }
                Message.Status.FAILURE -> {
                    Color.Red
                }
                else -> {
                    Color.Black
                }
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
                    .aspectRatio(1f / 1f),
                tint = iconTint
            )

            val fontWeight = if (lastMessage.isStatus(Message.Status.READ) || isFromMe) {
                FontWeight.Light
            } else {
                FontWeight.Bold
            }

            val modifierBody = Modifier
                .constrainAs(textMessage) {
                    start.linkTo(iconRead.end)
                    end.linkTo(unreadCount.start)
                    top.linkTo(textName.bottom)
                    width = Dimension.fillToConstraints
                }
                .padding(horizontal = 12.dp)

            if (messageInfo.isTyping) {
                Text(
                    text = "Typing....",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = fontWeight,
                    modifier = modifierBody
                )
            } else {
                when (lastMessage) {
                    is Message.TextMessages -> {
                        Text(
                            text = lastMessage.messageBody,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = fontWeight,
                            modifier = modifierBody
                        )
                    }
                    is Message.ImageMessages -> {
                        Text(
                            text = "[Image]",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = fontWeight,
                            modifier = modifierBody
                        )
                    }
                    else -> {}
                }
            }

            val unreadCountVisibility = if (messageInfo.unreadCount >= 1 && !isFromMe) {
                Visibility.Visible
            } else {
                Visibility.Invisible
            }

            UnreadCount(
                modifier = Modifier
                    .constrainAs(unreadCount) {
                        end.linkTo(parent.end, margin = 6.dp)
                        top.linkTo(textName.bottom, margin = 2.dp)
                        bottom.linkTo(textMessage.bottom, margin = 2.dp)
                        visibility = unreadCountVisibility
                        width = Dimension.wrapContent
                    }
                    .padding(horizontal = 3.dp),
                count = messageInfo.unreadCount
            )

            /*val imageChat = chatInfo.lastMessage.getImageChat()
            if (false) {
                Icon(
                    imageVector = Icons.Sharp.Image,
                    contentDescription = "",
                    modifier = modifierBody
                )
            } else {
                val subtitle = if (chatInfo.typingIds.contains(contact.id)) {
                    "Typing..."
                } else {
                    chatInfo.lastMessage.messageBody
                }

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = fontWeight,
                    modifier = modifierBody
                )
            }

            val unreadCountVisibility = if (chatInfo.unread >= 1 && !isFromMe) {
                Visibility.Visible
            } else {
                Visibility.Invisible
            }

            UnreadCount(
                modifier = Modifier
                    .constrainAs(unreadCount) {
                        end.linkTo(parent.end, margin = 6.dp)
                        top.linkTo(textName.bottom, margin = 2.dp)
                        bottom.linkTo(textMessage.bottom, margin = 2.dp)
                        visibility = unreadCountVisibility
                        width = Dimension.wrapContent
                    }
                    .padding(horizontal = 3.dp)
                    ,
                count = chatInfo.unread
            )

            ResponsiveText(
                text = DateUtils.generateDateChat(chatInfo.lastMessage.lastUpdate),
                textStyle = TextStyle(fontSize = 10.sp),
                modifier = Modifier
                    .constrainAs(textDate) {
                        end.linkTo(parent.end, margin = 6.dp)
                        start.linkTo(textName.end, margin = 6.dp)
                        top.linkTo(textName.top)
                        bottom.linkTo(textName.bottom)
                        width = Dimension.fillToConstraints
                    },
                textAlign = TextAlign.End
            )*/
        }
    }
}

@Composable
fun UnreadCount(modifier: Modifier, count: Int) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = count.toString(),
            fontSize = 8.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .background(Color.Red, shape = RoundedCornerShape(80))
                .padding(horizontal = 5.dp)

        )
    }
}

@Composable
fun ProfileScreen() {
    val navigationProvider = LocalMainProvider.current.navProvider()

    ColumnCenter {
        Text(text = "this is profile screen", modifier = Modifier.clickable {
            navigationProvider.back()
        })
    }
}