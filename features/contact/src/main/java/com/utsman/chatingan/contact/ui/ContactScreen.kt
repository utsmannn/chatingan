package com.utsman.chatingan.contact.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.utsman.chatingan.common.event.doOnEmpty
import com.utsman.chatingan.common.event.doOnFailure
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.doOnSuccess
import com.utsman.chatingan.common.ui.EmptyScreen
import com.utsman.chatingan.common.ui.FailureScreen
import com.utsman.chatingan.common.ui.LoadingScreen
import com.utsman.chatingan.common.ui.clickableRipple
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.navigation.LocalMainProvider
import com.utsman.chatingan.navigation.NavigationProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun ContactScreen(
    viewModel: ContactViewModel = getViewModel()
) {

    val navigationProvider = LocalMainProvider.current.navProvider()
    val contact by viewModel.getContacts().collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigationProvider.navigateToAddContact()
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.PersonAdd,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            )
        }
    ) {
        DefaultLayoutAppBar(
            title = "Contact",
            onBack = { navigationProvider.back() },
            content = {
                contact
                    .doOnEmpty { EmptyScreen() }
                    .doOnFailure { FailureScreen(message = it.message.orEmpty()) }
                    .doOnLoading { LoadingScreen() }
                    .doOnSuccess { contacts ->
                        ColumnCenter {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(contacts) { contact ->
                                    ContactItemScreen(
                                        contact = contact,
                                        onClick = {
                                            navigationProvider.navigateToChat(contact)
                                        }
                                    )
                                }
                            }
                        }
                    }
            }
        )
    }
}

@Composable
fun ContactItemScreen(
    modifier: Modifier = Modifier,
    contact: Contact,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickableRipple {
                onClick.invoke()
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
                model = contact.imageUrl,
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

            Text(
                text = contact.email,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .constrainAs(textMessage) {
                        start.linkTo(gh1)
                        end.linkTo(parent.end)
                        top.linkTo(textName.bottom)
                        width = Dimension.fillToConstraints
                    },
            )
        }
    }
}