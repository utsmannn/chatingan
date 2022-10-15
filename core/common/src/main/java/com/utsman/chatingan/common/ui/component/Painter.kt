package com.utsman.chatingan.common.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.utsman.chatingan.common.R

@Composable
fun IconResChatDone() : Painter {
    return painterResource(id = R.drawable.ic_round_done_24)
}

@Composable
fun IconResChatDoneAll() : Painter {
    return painterResource(id = R.drawable.ic_round_done_all_24)
}

@Composable
fun IconResChatDoneAllRead() : Painter {
    return painterResource(id = R.drawable.ic_round_done_read_24)
}

@Composable
fun IconResChatFailure() : Painter {
    return painterResource(id = R.drawable.ic_baseline_error_24)
}