package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MeshRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val chatDao = database.chatDao()
    val messageDao = database.messageDao()
    val meshNodeDao = database.meshNodeDao()

    val allThreads: Flow<List<ChatThreadEntity>> = chatDao.getAllThreads()
    val allNodes: Flow<List<MeshNodeEntity>> = meshNodeDao.getAllNodes()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _activeSos = MutableStateFlow(false)
    val activeSos: StateFlow<Boolean> = _activeSos.asStateFlow()

    private val _myProfileName = MutableStateFlow("Secure User")
    val myProfileName: StateFlow<String> = _myProfileName.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Seed initial data if empty
            // We can check if threads exist by observing or running a query
            // For simplicity, seed default threads & nodes
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val now = System.currentTimeMillis()
        val defaultThread = ChatThreadEntity(
            id = "chat_general",
            title = "#general (Local Mesh)",
            lastMessage = "Welcome to MeshChat! Fully offline P2P ready.",
            lastTimestamp = now - 60000,
            unreadCount = 2,
            isGroup = true,
            avatarUrl = "",
            peerPublicKey = "AES-256-GCM-KEY-1A",
            isPinned = true
        )
        val sosThread = ChatThreadEntity(
            id = "chat_sos",
            title = "🚨 Emergency SOS Channel",
            lastMessage = "Emergency broadcast frequency active.",
            lastTimestamp = now - 3600000,
            unreadCount = 0,
            isGroup = true,
            avatarUrl = "",
            peerPublicKey = "AES-256-GCM-KEY-SOS",
            isPinned = true
        )
        val peerThread = ChatThreadEntity(
            id = "chat_peer_1",
            title = "Alex Vance (Nearby)",
            lastMessage = "Hey! Are you connected to the mesh node?",
            lastTimestamp = now - 120000,
            unreadCount = 0,
            isGroup = false,
            avatarUrl = "",
            peerPublicKey = "AES-256-GCM-KEY-ALEX",
            isPinned = false
        )

        chatDao.insertThread(defaultThread)
        chatDao.insertThread(sosThread)
        chatDao.insertThread(peerThread)

        // Seed messages
        messageDao.insertMessage(
            ChatMessageEntity(
                chatId = "chat_general",
                senderId = "node_alex",
                senderName = "Alex Vance",
                messageText = "Welcome to MeshChat! Fully offline P2P ready.",
                timestamp = now - 60000,
                status = "READ",
                mediaType = "TEXT"
            )
        )
        messageDao.insertMessage(
            ChatMessageEntity(
                chatId = "chat_peer_1",
                senderId = "node_alex",
                senderName = "Alex Vance",
                messageText = "Hey! Are you connected to the mesh node?",
                timestamp = now - 120000,
                status = "READ",
                mediaType = "TEXT"
            )
        )

        // Seed mesh nodes
        meshNodeDao.insertNode(
            MeshNodeEntity(
                nodeId = "node_alex",
                name = "Alex Vance",
                rssi = -52,
                hopCount = 1,
                batteryLevel = 88,
                isConnected = true,
                lastSeen = now,
                ipAddress = "192.168.43.2"
            )
        )
        meshNodeDao.insertNode(
            MeshNodeEntity(
                nodeId = "node_sarah",
                name = "Sarah Connor (Relay)",
                rssi = -68,
                hopCount = 2,
                batteryLevel = 94,
                isConnected = true,
                lastSeen = now,
                ipAddress = "192.168.43.5"
            )
        )
        meshNodeDao.insertNode(
            MeshNodeEntity(
                nodeId = "node_david",
                name = "David K.",
                rssi = -82,
                hopCount = 3,
                batteryLevel = 45,
                isConnected = true,
                lastSeen = now - 5000,
                ipAddress = "192.168.43.12"
            )
        )
    }

    fun getMessages(chatId: String): Flow<List<ChatMessageEntity>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun sendMessage(chatId: String, text: String, mediaUrl: String? = null, mediaType: String = "TEXT") {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val msg = ChatMessageEntity(
                chatId = chatId,
                senderId = "me",
                senderName = _myProfileName.value,
                messageText = text,
                timestamp = now,
                status = "DELIVERED",
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                isEncrypted = true
            )
            messageDao.insertMessage(msg)

            // Update thread last message
            val thread = chatDao.getThreadById(chatId)
            if (thread != null) {
                chatDao.insertThread(
                    thread.copy(
                        lastMessage = if (text.isNotBlank()) text else "[$mediaType]",
                        lastTimestamp = now
                    )
                )
            }
        }
    }

    suspend fun startScanning() {
        _isScanning.value = true
        delay(3000) // simulate P2P radar scan
        val now = System.currentTimeMillis()
        meshNodeDao.insertNode(
            MeshNodeEntity(
                nodeId = "node_new_${now}",
                name = "Nearby Scout #${(10..99).random()}",
                rssi = (-50..-75).random(),
                hopCount = (1..2).random(),
                batteryLevel = (60..100).random(),
                isConnected = true,
                lastSeen = now,
                ipAddress = "192.168.43.${(10..50).random()}"
            )
        )
        _isScanning.value = false
    }

    fun toggleSos(enabled: Boolean) {
        _activeSos.value = enabled
    }

    fun updateProfileName(name: String) {
        _myProfileName.value = name
    }

    suspend fun createNewChat(id: String, title: String, isGroup: Boolean, peerKey: String) {
        withContext(Dispatchers.IO) {
            val newThread = ChatThreadEntity(
                id = id,
                title = title,
                lastMessage = "Secure channel established.",
                lastTimestamp = System.currentTimeMillis(),
                unreadCount = 0,
                isGroup = isGroup,
                avatarUrl = "",
                peerPublicKey = peerKey,
                isPinned = false
            )
            chatDao.insertThread(newThread)
        }
    }
}
