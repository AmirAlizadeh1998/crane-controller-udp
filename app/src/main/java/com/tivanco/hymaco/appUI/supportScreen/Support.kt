package com.tivanco.hymaco.appUI.supportScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.viewModel.ai.SupportViewModel

@Composable
fun Support(
    modifier: Modifier = Modifier,
    viewModel: SupportViewModel
) {
    val messages = viewModel.messages.collectAsState().value
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier.padding(12.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { msg ->
                ChatBubble(message = msg)
            }
        }
    }
}