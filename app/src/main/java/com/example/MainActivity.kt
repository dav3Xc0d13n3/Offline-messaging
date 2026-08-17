package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MeshChatViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MeshChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeshChatApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MeshChatApp(viewModel: MeshChatViewModel) {
    val threads by viewModel.threads.collectAsStateWithLifecycle()
    val nodes by viewModel.nodes.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val activeSos by viewModel.activeSos.collectAsStateWithLifecycle()
    val profileName by viewModel.myProfileName.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val activeCallPeer by viewModel.activeCallPeer.collectAsStateWithLifecycle()

    var currentRoute by remember { mutableStateOf("chat_list") }

    // If active voice call is ongoing
    if (activeCallPeer != null) {
        VoiceCallScreen(
            peerName = activeCallPeer!!,
            onEndCall = { viewModel.endCall() }
        )
        return
    }

    when {
        selectedChatId != null -> {
            val thread = threads.find { it.id == selectedChatId }
            val messages by viewModel.getMessagesForChat(selectedChatId!!).collectAsStateWithLifecycle(initialValue = emptyList())
            ChatDetailScreen(
                thread = thread,
                messages = messages,
                onBack = { viewModel.selectChat(null) },
                onSendMessage = { text, type -> viewModel.sendMessage(selectedChatId!!, text, null, type) },
                onStartCall = { name -> viewModel.startCall(name) }
            )
        }
        currentRoute == "radar" -> {
            NearbyRadarScreen(
                nodes = nodes,
                isScanning = isScanning,
                onStartScan = { viewModel.startScanning() },
                onBack = { currentRoute = "chat_list" },
                onConnectNode = { node ->
                    viewModel.startNewChat(node.name, false)
                }
            )
        }
        currentRoute == "topology" -> {
            MeshTopologyScreen(
                nodes = nodes,
                onBack = { currentRoute = "chat_list" }
            )
        }
        currentRoute == "channels" -> {
            ChannelsScreen(
                activeSos = activeSos,
                onToggleSos = { viewModel.toggleSos(it) },
                onOpenChat = { chatId -> viewModel.selectChat(chatId) },
                onBack = { currentRoute = "chat_list" }
            )
        }
        currentRoute == "pairing" -> {
            PairingScreen(
                profileName = profileName,
                onAddFriend = { friendName ->
                    viewModel.startNewChat(friendName, false)
                },
                onBack = { currentRoute = "chat_list" }
            )
        }
        currentRoute == "settings" -> {
            SettingsScreen(
                profileName = profileName,
                darkMode = darkMode,
                onUpdateName = { viewModel.updateProfileName(it) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onBack = { currentRoute = "chat_list" }
            )
        }
        else -> {
            ChatListScreen(
                threads = threads,
                onChatClick = { chatId -> viewModel.selectChat(chatId) },
                onNewChatClick = {
                    viewModel.startNewChat("New P2P Chat", false)
                },
                onNavigateRadar = { currentRoute = "radar" },
                onNavigateTopology = { currentRoute = "topology" },
                onNavigateChannels = { currentRoute = "channels" },
                onNavigateSettings = { currentRoute = "settings" },
                onNavigatePairing = { currentRoute = "pairing" }
            )
        }
    }
}
