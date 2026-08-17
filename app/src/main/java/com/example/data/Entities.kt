package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val isGroup: Boolean,
    val avatarUrl: String,
    val peerPublicKey: String,
    val isPinned: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val messageText: String,
    val timestamp: Long,
    val status: String, // SENT, DELIVERED, READ
    val mediaUrl: String? = null,
    val mediaType: String = "TEXT", // TEXT, IMAGE, VOICE, VIDEO, DOCUMENT
    val reactions: String = "",
    val isEncrypted: Boolean = true
)

@Entity(tableName = "mesh_nodes")
data class MeshNodeEntity(
    @PrimaryKey val nodeId: String,
    val name: String,
    val rssi: Int,
    val hopCount: Int,
    val batteryLevel: Int,
    val isConnected: Boolean,
    val lastSeen: Long,
    val ipAddress: String
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllThreads(): Flow<List<ChatThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThreadEntity)

    @Query("SELECT * FROM chat_threads WHERE id = :chatId")
    suspend fun getThreadById(chatId: String): ChatThreadEntity?
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, newStatus: String)
}

@Dao
interface MeshNodeDao {
    @Query("SELECT * FROM mesh_nodes ORDER BY rssi DESC")
    fun getAllNodes(): Flow<List<MeshNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: MeshNodeEntity)

    @Query("DELETE FROM mesh_nodes WHERE lastSeen < :cutoffTime")
    suspend fun removeStaleNodes(cutoffTime: Long)
}
