package com.anushmp.mywhatsapp.util

import android.content.Context
import android.net.Uri
import com.anushmp.mywhatsapp.data.db.MessageEntity
import com.anushmp.mywhatsapp.data.repository.MessageRepository
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ExportImportManager(private val repository: MessageRepository) {

    sealed class ExportState {
        object Idle : ExportState()
        data class InProgress(val percent: Int) : ExportState()
        object Done : ExportState()
        data class Error(val message: String) : ExportState()
    }

    sealed class ImportState {
        object Idle : ImportState()
        data class InProgress(val inserted: Int) : ImportState()
        data class Done(val inserted: Int) : ImportState()
        data class Error(val message: String) : ImportState()
    }

    companion object {
        private const val PAGE_SIZE = 500
        const val EXPORT_VERSION = 1
    }

    fun export(context: Context, uri: Uri): Flow<ExportState> = flow {
        emit(ExportState.InProgress(0))
        try {
            val totalCount = repository.count()
            val stream = context.contentResolver.openOutputStream(uri)
                ?: run { emit(ExportState.Error("Could not open output stream")); return@flow }

            stream.use { out ->
                JsonWriter(out.writer()).use { writer ->
                    writer.beginObject()
                    writer.name("version").value(EXPORT_VERSION)
                    writer.name("exportedAt").value(System.currentTimeMillis())
                    writer.name("messages")
                    writer.beginArray()

                    var lastId = 0L
                    var exported = 0
                    while (true) {
                        val page = repository.getPageAfter(lastId, PAGE_SIZE)
                        if (page.isEmpty()) break
                        for (msg in page) {
                            writer.beginObject()
                            writer.name("id").value(msg.id)
                            writer.name("content").value(msg.content)
                            writer.name("timestamp").value(msg.timestamp)
                            writer.endObject()
                        }
                        exported += page.size
                        lastId = page.last().id
                        val pct = if (totalCount > 0) (exported * 100 / totalCount) else 100
                        emit(ExportState.InProgress(pct))
                    }

                    writer.endArray()
                    writer.endObject()
                }
            }
            emit(ExportState.Done)
        } catch (e: Exception) {
            emit(ExportState.Error(e.message ?: "Export failed"))
        }
    }.flowOn(Dispatchers.IO)

    fun import(context: Context, uri: Uri): Flow<ImportState> = flow {
        emit(ImportState.InProgress(0))
        try {
            val stream = context.contentResolver.openInputStream(uri)
                ?: run { emit(ImportState.Error("Could not open input stream")); return@flow }

            var errorMessage: String? = null
            var totalInserted = 0

            stream.use { input ->
                JsonReader(input.reader()).use { reader ->
                    reader.beginObject()
                    var version = 0
                    val buffer = mutableListOf<MessageEntity>()

                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "version" -> version = reader.nextInt()
                            "exportedAt" -> reader.skipValue()
                            "messages" -> {
                                if (version > EXPORT_VERSION) {
                                    reader.skipValue()
                                    errorMessage = "Unsupported export version: $version"
                                } else {
                                    reader.beginArray()
                                    while (reader.hasNext()) {
                                        var id = 0L
                                        var content = ""
                                        var timestamp = 0L
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            when (reader.nextName()) {
                                                "id" -> id = reader.nextLong()
                                                "content" -> content = reader.nextString()
                                                "timestamp" -> timestamp = reader.nextLong()
                                                else -> reader.skipValue()
                                            }
                                        }
                                        reader.endObject()
                                        buffer.add(
                                            MessageEntity(id = id, content = content, timestamp = timestamp)
                                        )
                                        if (buffer.size >= PAGE_SIZE) {
                                            repository.insertAll(buffer)
                                            totalInserted += buffer.size
                                            buffer.clear()
                                            emit(ImportState.InProgress(totalInserted))
                                        }
                                    }
                                    reader.endArray()
                                    if (buffer.isNotEmpty()) {
                                        repository.insertAll(buffer)
                                        totalInserted += buffer.size
                                        buffer.clear()
                                    }
                                }
                            }
                            else -> reader.skipValue()
                        }
                    }
                    if (errorMessage == null) reader.endObject()
                }
            }

            if (errorMessage != null) {
                emit(ImportState.Error(errorMessage!!))
            } else {
                emit(ImportState.Done(totalInserted))
            }
        } catch (e: Exception) {
            emit(ImportState.Error(e.message ?: "Import failed"))
        }
    }.flowOn(Dispatchers.IO)
}
