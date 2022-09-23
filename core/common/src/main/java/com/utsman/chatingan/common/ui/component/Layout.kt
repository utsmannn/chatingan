package com.utsman.chatingan.common.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun ColumnCenter(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun DefaultTopBar(
    title: String,
    onBack: () -> Unit = {}
) {
    TopAppBar(backgroundColor = MaterialTheme.colors.primary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            // Title
            //Text(title)

            // Navigation Icon
            if (onBack != {}) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
            }
        }
    }
}

@Composable
fun DefaultLayoutAppBar(
    title: String,
    onBack: () -> Unit = {},
    content: @Composable PaddingValues.() -> Unit
) {
    Scaffold(
        topBar = {
            if (onBack == {}) {
                TopAppBar(
                    title = {
                        Text(text = title)
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(text = title)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                        }
                    }
                )
            }
        },
        content = content
    )
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}