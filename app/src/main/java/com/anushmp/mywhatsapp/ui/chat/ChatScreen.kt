package com.anushmp.mywhatsapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.anushmp.mywhatsapp.ui.chat.components.ChatInputBar
import com.anushmp.mywhatsapp.ui.chat.components.MessageBubble
import com.anushmp.mywhatsapp.util.ExportImportManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val messages = viewModel.messages.collectAsLazyPagingItems()
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(exportState) {
        when (val s = exportState) {
            is ExportImportManager.ExportState.Done -> {
                snackbarHostState.showSnackbar("Export complete")
                viewModel.resetExportState()
            }
            is ExportImportManager.ExportState.Error -> {
                snackbarHostState.showSnackbar("Export failed: ${s.message}")
                viewModel.resetExportState()
            }
            else -> {}
        }
    }

    LaunchedEffect(importState) {
        when (val s = importState) {
            is ExportImportManager.ImportState.Done -> {
                snackbarHostState.showSnackbar("Imported ${s.inserted} messages")
                viewModel.resetImportState()
            }
            is ExportImportManager.ImportState.Error -> {
                snackbarHostState.showSnackbar("Import failed: ${s.message}")
                viewModel.resetImportState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MyWhatsapp",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export") },
                            onClick = { showMenu = false; onExport() }
                        )
                        DropdownMenuItem(
                            text = { Text("Import") },
                            onClick = { showMenu = false; onImport() }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val isActive = exportState is ExportImportManager.ExportState.InProgress
                || importState is ExportImportManager.ImportState.InProgress

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isActive) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    count = messages.itemCount,
                    key = messages.itemKey { it.id }
                ) { index ->
                    messages[index]?.let { MessageBubble(message = it) }
                }
            }

            ChatInputBar(
                modifier = Modifier.imePadding(),
                onSend = { content ->
                    viewModel.sendMessage(content)
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                }
            )
        }
    }
}
