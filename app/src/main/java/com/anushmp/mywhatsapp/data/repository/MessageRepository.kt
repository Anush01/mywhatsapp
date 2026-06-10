package com.anushmp.mywhatsapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.anushmp.mywhatsapp.data.db.MessageDao
import com.anushmp.mywhatsapp.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val dao: MessageDao) {

    fun getPager(): Flow<PagingData<MessageEntity>> =
        Pager(PagingConfig(pageSize = 30, enablePlaceholders = false)) {
            dao.getMessagesPaged()
        }.flow

    suspend fun insert(content: String) {
        dao.insert(MessageEntity(content = content, timestamp = System.currentTimeMillis()))
    }

    suspend fun insertAll(messages: List<MessageEntity>) = dao.insertAll(messages)

    suspend fun count(): Int = dao.count()

    suspend fun getPageAfter(afterId: Long, limit: Int): List<MessageEntity> =
        dao.getPageAfter(afterId, limit)
}
