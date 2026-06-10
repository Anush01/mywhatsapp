package com.anushmp.mywhatsapp.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    // Newest first — used with reverseLayout=true so index 0 appears at the visual bottom
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getMessagesPaged(): PagingSource<Int, MessageEntity>

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun count(): Int

    // Cursor-based pagination avoids O(n²) OFFSET scans — each page is O(log n) via B-tree index
    @Query("SELECT * FROM messages WHERE id > :afterId ORDER BY id ASC LIMIT :limit")
    suspend fun getPageAfter(afterId: Long, limit: Int): List<MessageEntity>
}
