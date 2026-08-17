package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessageEntity
import com.example.data.ChatThreadEntity
import com.example.data.MeshNodeEntity
import com.example.data.MeshRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeshChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MeshRepository(application)

    val threads: StateFlow<List<ChatThreadEntity>> = repository.allThreads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nodes: StateFlow<List<MeshNodeEntity>> = repository.allNodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isScanning: StateFlow<Boolean> = repository.isScanning
    val activeSos: StateFlow<Boolean> = repository.activeSos
    val myProfileName: StateFlow<String> = repository.myProfileName

    private val _darkMode = MutableStateFlow(true)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _activeCallPeer = MutableStateFlow<String?>(null)
    val activeCallPeer: StateFlow<String?> = _activeCallPeer.asStateFlow()

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>> {
        return repository.getMessages(chatId)
    }

    fun sendMessage(chatId: String, text: String, mediaUrl: String? = null, mediaType: String = "TEXT") {
        viewModelScope.launch {
            repository.sendMessage(chatId, text, mediaUrl, mediaType)
        }
    }

    fun startScanning() {
        viewModelScope.launch {
            repository.startScanning()
        }
    }

    fun toggleSos(enabled: Boolean) {
        repository.toggleSos(enabled)
    }

    fun updateProfileName(name: String) {
        repository.updateProfileName(name)
    }

    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
    }

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
    }

    fun startNewChat(title: String, isGroup: Boolean) {
        viewModelScope.launch {
            val id = "chat_${System.currentTimeMillis()}"
            repository.createNewChat(id, title, isGroup, "KEY_${System.currentTimeMillis().toString(36)}")
            _selectedChatId.value = id
        }
    }

    fun startCall(peerName: String) {
        _activeCallPeer.value = peerName
    }

    fun endCall() {
        _activeCallPeer.value = null
    }
}
