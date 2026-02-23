package com.safeflow

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeflow.ui.theme.SafeFlowTheme
import com.safeflow.ui.theme.SoftBlue
import com.safeflow.ui.theme.SoftGray
import com.safeflow.ui.theme.SoftMintGreen
import com.safeflow.ui.theme.DarkGray
import com.safeflow.ui.theme.SoftRed

class MainActivity : ComponentActivity() {

    private lateinit var blockedSitesManager: BlockedSitesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        blockedSitesManager = BlockedSitesManager(this)

        setContent {
            SafeFlowTheme {
                HomeScreen(
                    onAddSite = { site ->
                        val added = blockedSitesManager.addBlockedSite(site)
                        if (added) {
                            Toast.makeText(this, "✓ Site bloqué: $site", Toast.LENGTH_SHORT).show()
                            true
                        } else {
                            Toast.makeText(this, "Site déjà bloqué ou invalide", Toast.LENGTH_SHORT).show()
                            false
                        }
                    },
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    isServiceActive = isAccessibilityServiceEnabled()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Force recomposition when returning
        recreate()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val service = "${packageName}/${MyAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains(service) == true
        } catch (e: Exception) {
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSite: (String) -> Boolean,
    onSettingsClick: () -> Unit,
    isServiceActive: Boolean
) {
    var siteInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Settings Icon (Top Right)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = SoftGray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: SafeFlow Title
                Text(
                    text = "SafeFlow",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    color = SoftGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Center: Shield Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    // HUGE Shield Icon
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = if (isServiceActive) SoftBlue else SoftGray,
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Protection Text
                    Text(
                        text = "Protection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        color = SoftGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Text
                    Text(
                        text = if (isServiceActive) "ACTIVE" else "INACTIVE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isServiceActive) SoftMintGreen else SoftRed
                    )
                }

                // Bottom: Input Card & Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Input Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            OutlinedTextField(
                                value = siteInput,
                                onValueChange = { siteInput = it },
                                label = { Text("Ajouter un site (ex: tiktok.com)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftBlue,
                                    unfocusedBorderColor = SoftGray
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (siteInput.isNotBlank()) {
                                        val success = onAddSite(siteInput)
                                        if (success) {
                                            siteInput = ""
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftBlue,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "BLOQUER",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Two Pill Buttons (Optional features)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* Scan Device feature */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5),
                                contentColor = DarkGray
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "Scan Device",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { /* VPN Access feature */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5),
                                contentColor = DarkGray
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "VPN Access",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
