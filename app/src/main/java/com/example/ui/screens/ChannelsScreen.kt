package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    activeSos: Boolean,
    onToggleSos: (Boolean) -> Unit,
    onOpenChat: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Channels & SOS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SOS Emergency Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (activeSos) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (activeSos) Color.White else Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Emergency SOS Broadcast",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (activeSos) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = activeSos,
                            onCheckedChange = onToggleSos
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (activeSos) "🚨 SOS Beacon Active! Broadcasting emergency distress signal to all nearby mesh nodes within range."
                        else "Toggle to broadcast high-priority emergency distress signal across all mesh relay channels.",
                        fontSize = 13.sp,
                        color = if (activeSos) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Local Community Channels", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    ChannelItem(
                        name = "#general",
                        description = "Open public channel for all nearby mesh participants",
                        icon = Icons.Default.Public,
                        onClick = { onOpenChat("chat_general") }
                    )
                }
                item {
                    ChannelItem(
                        name = "#emergency",
                        description = "Dedicated safety and weather alert channel",
                        icon = Icons.Default.Shield,
                        onClick = { onOpenChat("chat_sos") }
                    )
                }
                item {
                    ChannelItem(
                        name = "#tech-mesh",
                        description = "Discussing P2P routing and relay node optimization",
                        icon = Icons.Default.Router,
                        onClick = { onOpenChat("chat_general") }
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelItem(name: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
