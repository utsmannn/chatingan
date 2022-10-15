package com.utsman.chatingan.chat.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import coil.compose.AsyncImage
import com.utsman.chatingan.chat.routes.BackPassChat
import com.utsman.chatingan.common.DateUtils
import com.utsman.chatingan.common.ui.component.DURATION_ANIMATION_TRANSITION
import com.utsman.chatingan.common.ui.component.Gray50
import com.utsman.chatingan.common.ui.component.IconResChatDone
import com.utsman.chatingan.common.ui.component.IconResChatDoneAll
import com.utsman.chatingan.common.ui.component.IconResChatFailure
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.utils.ChatinganDividerUtils
import com.utsman.chatingan.navigation.LocalMainProvider
import com.utsman.chatingan.navigation.NavigationProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

private val AppBarHeight = 56.dp

private const val ChatRounded = 12

private val ChatShapeSender = RoundedCornerShape(
    topStartPercent = ChatRounded,
    bottomEndPercent = ChatRounded,
    bottomStartPercent = ChatRounded
)

private val ChatShapeReceiver = RoundedCornerShape(
    topEndPercent = ChatRounded,
    bottomEndPercent = ChatRounded,
    bottomStartPercent = ChatRounded
)

@Composable
fun ChatScreen(
    contact: Contact,
    viewModel: ChatViewModel = getViewModel(),
    backPassChat: BackPassChat = get(),
) {
    val navigationProvider = LocalMainProvider.current.navProvider()

    val imageResult by viewModel.imageFileState.collectAsState()
    val currentBackPass by backPassChat.currentBack.collectAsState()

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

                BottomBarChat(
                    modifier = Modifier
                        .constrainAs(inputBox) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                            start.linkTo(parent.start)
                            height = Dimension.wrapContent
                        }
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 12.dp),
                    viewModel = viewModel,
                    navigationProvider = navigationProvider,
                    onSend = {
                        when (currentBackPass) {
                            "CAMERA_VIEW" -> {
                                val imageFile = imageResult.getOrThrow()
                                //viewModel.sendImage(contact, it, imageFile)
                                //Toast.makeText(context, "image", Toast.LENGTH_SHORT).show()
                                //backPassChat.clearBackFrom()
                            }
                            else -> {
                                val textMessage = Message.buildTextMessage(contact) {
                                    message = it
                                }
                                viewModel.sendMessage(contact, textMessage)
                            }
                        }
                    }
                )

                ChatContent(
                    modifier = Modifier
                        .constrainAs(chatBox) {
                            top.linkTo(parent.top)
                            bottom.linkTo(inputBox.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints
                        },
                    contact = contact,
                    viewModel = viewModel,
                    backPassChat = backPassChat
                )
            }
        }
    )
}

@Composable
fun ChatContent(
    modifier: Modifier,
    contact: Contact,
    viewModel: ChatViewModel,
    backPassChat: BackPassChat
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val imageResult by viewModel.imageFileState.collectAsState()
    val currentBackPass by backPassChat.currentBack.collectAsState()

    var isUpdatedMessage by remember {
        mutableStateOf(false)
    }

    val activityProvider = LocalMainProvider.current

    val pagingMessages: LazyPagingItems<Message> = viewModel.pagingData.collectAsLazyPagingItems()

    scope.launch {
        delay(DURATION_ANIMATION_TRANSITION.toLong())
        viewModel.listenForTyping(contact)
        viewModel.getMessages(contact)
    }


    val refreshMessage = {
        scope.launch {
            isUpdatedMessage = false
            pagingMessages.refresh()
            delay(500)
            scrollState.scrollToItem(0)
        }
    }

    scope.launch {
        activityProvider.chatingan().onMessageUpdate(contact) {
            isUpdatedMessage = true
            val messageIndex = scrollState.firstVisibleItemIndex
            if (messageIndex == 0) {
                refreshMessage()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (isUpdatedMessage) {
                FloatingActionButton(
                    modifier = Modifier.scale(0.7f),
                    onClick = {
                        scope.launch {
                            refreshMessage()
                        }
                    },
                    content = {
                        Icon(
                            contentDescription = null,
                            tint = Color.White,
                            imageVector = Icons.Default.ArrowCircleDown
                        )
                    }
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            state = scrollState,
            content = {
                itemsIndexed(
                    items = pagingMessages,
                    key = { _, item ->
                        item.getChildId()
                    },
                    itemContent = { index, item ->
                        if (index == 0) {
                            if (scrollState.firstVisibleItemIndex == 0) {
                                refreshMessage()
                            }
                        }

                        if (item != null) {
                            MessageItem(
                                contact = contact,
                                message = item,
                                viewModel = viewModel
                            )
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun MessageItem(
    contact: Contact,
    message: Message,
    viewModel: ChatViewModel,
    withDivider: Boolean = true
) {
    when (message) {
        is Message.TextMessages -> TextMessageItem(message = message) {
            viewModel.markAsRead(contact, message)
        }
        is Message.DividerMessage -> if (withDivider) {
            DividerMessageItem(contact, message, viewModel)
        }
        else -> Column {}
    }
}

@Composable
fun TopBarChat(
    contact: Contact,
    viewModel: ChatViewModel,
    onClick: () -> Unit
) {
    val receiverTypingState by viewModel.getTyping(contact.id).collectAsState(false)

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
                    model = contact.imageUrl,
                    contentDescription = "",
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
    modifier: Modifier,
    viewModel: ChatViewModel,
    navigationProvider: NavigationProvider,
    onSend: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val text by viewModel.textState.collectAsState("")
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
    ) {

        TextField(
            modifier = Modifier
                .weight(3f)
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            trailingIcon = {
                Icon(
                    Icons.Filled.PhotoCamera,
                    "",
                    tint = Color.LightGray,
                    modifier = Modifier.clickable {
                        navigationProvider.navigateToCamera()
                    })
            },
            value = text,
            onValueChange = {
                viewModel.setText(it)
            },
            colors = TextFieldDefaults.textFieldColors(
                disabledTextColor = Color.Transparent,
                backgroundColor = Color.Gray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        Button(
            modifier = Modifier.padding(start = 6.dp),
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
fun TextMessageItem(
    message: Message.TextMessages,
    onRead: () -> Unit
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

        val messageStatus = message.status
        val isFromMe = message.senderId == Chatingan.getInstance().getConfiguration().contact.id

        if (!isFromMe && messageStatus != Message.Status.READ) {
            onRead.invoke()
        }

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
                        text = DateUtils.toReadable(message.date),
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
                    )

                    val sizeVisibility = if (isFromMe) {
                        16.dp
                    } else {
                        0.dp
                    }

                    if (isFromMe) {
                        val iconReadPainter = when (message.getChildStatus()) {
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

                        val iconTint = when (message.getChildStatus()) {
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

                        Icon(
                            painter = iconReadPainter,
                            contentDescription = "",
                            modifier = Modifier
                                .size(sizeVisibility)
                                .padding(start = 3.dp),
                            tint = iconTint
                        )
                    }
                }

                val modifierBody = Modifier
                    .constrainAs(messageText) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(indicatorRow.top)
                    }

                Text(
                    text = message.messageBody,
                    modifier = modifierBody
                        .widthIn(min = 60.dp)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun DividerMessageItem(
    contact: Contact,
    message: Message.DividerMessage,
    viewModel: ChatViewModel
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MessageItem(contact, message.message, viewModel, false)

        Text(
            text = ChatinganDividerUtils.generateDateDividerText(message.superDate),
            modifier = Modifier
                .background(Gray50, shape = RoundedCornerShape(6.dp))
                .padding(6.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Light
        )
    }
}
