package com.anushmp.mywhatsapp.ui.chat

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anushmp.mywhatsapp.data.db.MessageDatabase
import com.anushmp.mywhatsapp.data.db.MessageEntity
import com.anushmp.mywhatsapp.data.repository.MessageRepository
import com.anushmp.mywhatsapp.util.ExportImportManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = MessageRepository(MessageDatabase.getInstance(context).messageDao())
    private val exportImportManager = ExportImportManager(repository)

    val messages: Flow<PagingData<MessageEntity>> = repository.getPager().cachedIn(viewModelScope)

    private val _exportState = MutableStateFlow<ExportImportManager.ExportState>(
        ExportImportManager.ExportState.Idle
    )
    val exportState: StateFlow<ExportImportManager.ExportState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<ExportImportManager.ImportState>(
        ExportImportManager.ImportState.Idle
    )
    val importState: StateFlow<ExportImportManager.ImportState> = _importState.asStateFlow()

    fun sendMessage(content: String) {
        viewModelScope.launch { repository.insert(content) }
    }

    fun exportMessages(uri: Uri) {
        viewModelScope.launch {
            exportImportManager.export(context, uri).collect { _exportState.value = it }
        }
    }

    fun importMessages(uri: Uri) {
        viewModelScope.launch {
            exportImportManager.import(context, uri).collect { _importState.value = it }
        }
    }

    fun resetExportState() { _exportState.value = ExportImportManager.ExportState.Idle }
    fun resetImportState() { _importState.value = ExportImportManager.ImportState.Idle }
}
