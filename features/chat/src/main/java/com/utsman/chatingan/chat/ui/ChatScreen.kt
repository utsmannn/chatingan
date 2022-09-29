package com.utsman.chatingan.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.defaultCompose
import com.utsman.chatingan.common.event.doOnIdle
import com.utsman.chatingan.common.event.doOnSuccess
import com.utsman.chatingan.common.ui.component.Gray50
import com.utsman.chatingan.common.ui.component.IconResChatDone
import com.utsman.chatingan.common.ui.component.IconResChatDoneAll
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.utils.DateUtils
import com.utsman.chatingan.sdk.utils.isAllRead
import com.utsman.chatingan.sdk.utils.isFromMe
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

private val AppBarHeight = 56.dp

private const val chatRounded = 12

private val ChatShapeSender = RoundedCornerShape(
    topStartPercent = chatRounded,
    bottomEndPercent = chatRounded,
    bottomStartPercent = chatRounded
)

private val ChatShapeReceiver = RoundedCornerShape(
    topEndPercent = chatRounded,
    bottomEndPercent = chatRounded,
    bottomStartPercent = chatRounded
)

@Composable
fun ChatScreen(
    contact: Contact,
    navigationProvider: NavigationProvider = get(),
    viewModel: ChatViewModel = getViewModel()
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val chatState by viewModel.chatState.collectAsState()

    Scaffold(
        topBar = {
            TopBarChat(
                contact = contact,
                viewModel = viewModel,
                onClick = {
                    navigationProvider.back()
                }
            )
        },
        content = {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (chatBox, inputBox) = createRefs()

                Box(
                    modifier = Modifier
                        .constrainAs(inputBox) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                            start.linkTo(parent.start)
                            height = Dimension.wrapContent
                        }
                ) {
                    BottomBarChat(
                        contact = contact,
                        viewModel = viewModel,
                        onSend = {
                            viewModel.sendMessage(contact, it)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .constrainAs(chatBox) {
                            top.linkTo(parent.top)
                            bottom.linkTo(inputBox.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints
                        }
                ) {
                    chatState.defaultCompose()
                        .doOnIdle {
                            viewModel.getChat(contact)
                        }
                        .doOnSuccess {
                            viewModel.listenForTyping(contact)

                            scope.launch {
                                scrollState.scrollToItem(it.messages.size)
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = scrollState,
                                content = {
                                    items(it.messages) { item ->
                                        if (item.type == MessageChat.Type.DIVIDER) {
                                            DividerScreen(messageChat = item)
                                        } else {
                                            viewModel.readChat(contact, item)
                                            MessageItem(messageChat = item)
                                        }
                                    }
                                })
                        }
                }
            }
        }
    )
}


@Composable
fun TopBarChat(
    contact: Contact,
    viewModel: ChatViewModel,
    onClick: () -> Unit
) {
    val receiverTypingState by viewModel.receiverTyping.collectAsState()

    val backgroundColor = MaterialTheme.colors.primarySurface
    Surface(
        color = backgroundColor,
        contentColor = contentColorFor(backgroundColor),
        elevation = AppBarDefaults.TopAppBarElevation,
        shape = RectangleShape,
        modifier = Modifier
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppBarDefaults.ContentPadding)
                .height(AppBarHeight),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            content = {
                IconButton(onClick = onClick) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                }
                AsyncImage(
                    model = contact.image,
                    contentDescription = contact.id,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                )

                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.h6,
                        overflow = TextOverflow.Ellipsis
                    )
                    AnimatedVisibility(
                        visible = receiverTypingState,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Text(text = "Typing...", fontSize = 12.sp)
                    }
                }

            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomBarChat(
    viewModel: ChatViewModel,
    contact: Contact,
    onSend: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val text by viewModel.textState.collectAsState("")
    val scope = rememberCoroutineScope()

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
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                value = text,
                onValueChange = {
                    viewModel.setText(it)
                }
            )
        }

        Button(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            onClick = {
                onSend.invoke(text)
                viewModel.setText("")
                keyboardController?.hide()
                focusManager.clearFocus(true)
            },
            content = {
                Text(text = "send")
            })
    }
}

@Composable
fun MessageItem(
    messageChat: MessageChat
) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .safeContentPadding()
    ) {
        val (boxSender, boxReceiver, messageBoxSender, messageBoxReceiver) = createRefs()
        val gh1 = createGuidelineFromStart(0.3f)
        val gh2 = createGuidelineFromEnd(0.3f)

        val isHasRead = messageChat.isAllRead()
        val isFromMe = messageChat.isFromMe(Chatingan.getInstance().config)

        val containerModifier = if (isFromMe) {
            Modifier
                .constrainAs(boxSender) {
                    start.linkTo(gh1)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .wrapContentSize(align = Alignment.TopEnd)

        } else {
            Modifier
                .constrainAs(boxReceiver) {
                    start.linkTo(parent.start)
                    end.linkTo(gh2)
                    width = Dimension.fillToConstraints
                }
                .wrapContentSize(align = Alignment.TopStart)
        }

        val messageModifier = if (isFromMe) {
            Modifier
                .constrainAs(messageBoxSender) {
                    end.linkTo(parent.end)
                }
                .background(Color.Magenta, shape = ChatShapeSender)

        } else {
            Modifier
                .constrainAs(messageBoxReceiver) {
                    start.linkTo(parent.start)
                }
                .background(Color.Gray, shape = ChatShapeReceiver)
        }

        Column(
            modifier = containerModifier
        ) {
            ConstraintLayout(
                modifier = messageModifier
            ) {
                val (messageText, indicatorRow) = createRefs()
                Row(
                    modifier = Modifier
                        .constrainAs(indicatorRow) {
                            alpha = 0.7f
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .widthIn(min = 60.dp)
                        .padding(end = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.toReadable(messageChat.lastUpdate),
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
                    )

                    val sizeVisibility = if (isFromMe) {
                        16.dp
                    } else {
                        0.dp
                    }

                    if (isFromMe) {
                        val iconReadPainter = if (isHasRead) {
                            IconResChatDoneAll()
                        } else {
                            IconResChatDone()
                        }

                        Icon(
                            painter = iconReadPainter,
                            contentDescription = "",
                            modifier = Modifier
                                .size(sizeVisibility)
                                .padding(start = 3.dp)
                        )
                    }
                }

                Text(
                    text = messageChat.messageBody,
                    modifier = Modifier
                        .constrainAs(messageText) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(indicatorRow.top)
                        }
                        .widthIn(min = 60.dp)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )


            }
        }
    }
}

@Composable
fun DividerScreen(messageChat: MessageChat) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = messageChat.messageBody,
            modifier = Modifier
                .background(Gray50, shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Light
        )
    }
}

/*
@Preview
@Composable
fun PreviewChat() {
    ChatScreen(contact = Contact())
}*/
