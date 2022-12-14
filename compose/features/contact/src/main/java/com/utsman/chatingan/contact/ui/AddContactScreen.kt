package com.utsman.chatingan.contact.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.utsman.chatingan.common.event.doOnLoading
import com.utsman.chatingan.common.event.doOnSuccess
import com.utsman.chatingan.common.ui.component.ChatinganText
import com.utsman.chatingan.common.ui.component.ColumnCenter
import com.utsman.chatingan.common.ui.component.DefaultLayoutAppBar
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.utils.ChatinganQrUtils
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.pair.ContactPairListener
import com.utsman.chatingan.lib.utils.isValid
import com.utsman.chatingan.navigation.LocalMainProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = getViewModel()
) {

    val navigationProvider = LocalMainProvider.current.navProvider()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.Expanded }
    )

    val coroutineScope = rememberCoroutineScope()

    var currentPairContact: Contact by remember {
        mutableStateOf(Contact.empty())
    }

    val meContact: Contact = remember {
        Chatingan.getInstance().getContact()
    }

    Chatingan
        .getInstance()
        .getChatinganQr()
        .setPairListener(object : ContactPairListener {
            override fun onPairSuccess(contact: Contact) {
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                navigationProvider.back()
            }

            override fun onPairFailed(throwable: Throwable) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
            }
        })

    BackHandler(sheetState.isVisible) {
        coroutineScope.launch {
            sheetState.animateTo(ModalBottomSheetValue.Hidden)
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            val content = result.contents
            if (content != null) {
                val contact = ChatinganQrUtils.generateContactFromPair(content)

                currentPairContact = contact

                coroutineScope.launch {
                    delay(500)
                    if (currentPairContact.isValid()) {
                        sheetState.animateTo(ModalBottomSheetValue.Expanded)
                    } else {
                        sheetState.hide()
                    }
                }
            }

        }
    )

    val scanOptions = remember {
        ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan a QR")
            captureActivity = QrCaptureActivity::class.java
        }
    }

    DefaultLayoutAppBar(
        title = "Pair contact",
        onBack = { navigationProvider.back() }
    ) {
        ModalBottomSheetLayout(
            modifier = Modifier.fillMaxSize(),
            sheetContent = {
                BottomSheetContact(
                    contact = currentPairContact,
                    viewModel = viewModel
                ) {
                    sheetState.hide()
                }
            },
            sheetState = sheetState,
            sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            ColumnCenter {
                ContactView(contact = meContact)
                Spacer(modifier = Modifier.height(12.dp))
                val qrBitmap = Chatingan
                    .getInstance()
                    .getChatinganQr()
                    .generateQrContact()

                AsyncImage(
                    model = qrBitmap,
                    contentDescription = ""
                )

                Button(onClick = { scanLauncher.launch(scanOptions) }) {
                    ChatinganText(text = "Scan QR")
                }
            }
        }
    }
}

@Composable
fun BottomSheetContact(
    contact: Contact,
    viewModel: AddContactViewModel,
    onBack: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isContactExist by viewModel.checkContactIsExist(contact).collectAsState()

    var isExist by remember {
        mutableStateOf(false)
    }
    var savedButtonEnabled by remember {
        mutableStateOf(true)
    }

    viewModel.checkContactIsExist(contact)

    isContactExist
        .doOnLoading {
            savedButtonEnabled = false
        }
        .doOnSuccess {
            savedButtonEnabled = true
            isExist = it
        }

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .padding(top = 1.dp)
    ) {
        ColumnCenter(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ContactView(contact = contact)
            Spacer(modifier = Modifier.height(12.dp))
            if (isExist) {
                Spacer(modifier = Modifier.height(20.dp))
                ChatinganText(text = "Contact has existing")
            }
            Button(onClick = {
                coroutineScope.launch {
                    if (isExist) {
                        onBack.invoke()
                    } else {
                        viewModel.addContact(contact)
                            .collect {
                                it.doOnSuccess {
                                    if (!isExist) {
                                        onBack.invoke()
                                    }
                                }
                            }
                    }
                }
            }, enabled = savedButtonEnabled) {
                val text = if (isExist) {
                    "Back"
                } else {
                    "Save contact"
                }
                ChatinganText(text = text)
            }
        }
    }
}

@Composable
fun ContactView(contact: Contact) {
    ColumnCenter(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        AsyncImage(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            model = contact.imageUrl,
            contentDescription = ""
        )

        ChatinganText(text = contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 4.sp)
        ChatinganText(text = contact.email, fontWeight = FontWeight.Light)
    }
}
