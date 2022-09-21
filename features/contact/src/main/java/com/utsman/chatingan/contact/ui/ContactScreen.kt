package com.utsman.chatingan.contact.ui

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.utsman.chatingan.navigation.NavigationProvider
import com.utsman.chatingan.common.event.composeStateOfMerge
import com.utsman.chatingan.common.ui.EmptyScreen
import com.utsman.chatingan.common.ui.clickableRipple
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.sdk.data.entity.Contact
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun ContactItemScreen(
    contact: Contact,
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
                text = contact.id,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun ContactScreen(
    navigationProvider: NavigationProvider = get(),
    viewModel: ContactViewModel = getViewModel()
) {
    DefaultLayoutAppBar(
        title = "Contact",
        onBack = { navigationProvider.back() },
        content = {
            viewModel.userState
                .composeStateOfMerge(other = viewModel.contactState) { user, contact ->
                    LazyColumn {
                        val filteredContact = contact.filter {
                            it.id != user.id
                        }
                        if (filteredContact.isNotEmpty()) {
                            items(filteredContact) {
                                if (it.id != user.id) {
                                    ContactItemScreen(
                                        contact = it,
                                        onClick = { contact ->
                                            navigationProvider.navigateToChat(contact)
                                        }
                                    )
                                }
                            }
                        } else {
                            item { EmptyScreen() }
                        }
                    }
                }
        }
    )
}