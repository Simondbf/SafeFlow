package com.safeflow

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.safeflow.ui.theme.SafeFlowTheme
import com.safeflow.ui.theme.SoftBlue
import com.safeflow.ui.theme.SoftGray
import com.safeflow.ui.theme.DarkGray
import kotlinx.coroutines.launch

class IntroActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private val adminResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "✓ Protection Activée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if first run
        val prefs = getSharedPreferences("SafeFlowPrefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)

        if (!isFirstRun) {
            startMainActivity()
            return
        }

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        setContent {
            SafeFlowTheme {
                OnboardingScreen(
                    onFinish = {
                        prefs.edit().putBoolean("isFirstRun", false).apply()
                        startMainActivity()
                    },
                    onOpenAccessibilitySettings = {
                        openAccessibilitySettings()
                    },
                    onActivateDeviceAdmin = {
                        activateDeviceAdmin()
                    },
                    onOpenDiscord = {
                        openDiscord()
                    },
                    isAccessibilityEnabled = { isAccessibilityServiceEnabled() },
                    isDeviceAdminActive = { devicePolicyManager.isAdminActive(adminComponent) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Trigger recomposition when returning from settings
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

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun activateDeviceAdmin() {
        try {
            if (!devicePolicyManager.isAdminActive(adminComponent)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "SafeFlow a besoin de cette permission pour empêcher la désinstallation."
                )
                adminResultLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Protection déjà activée", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openDiscord() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://discord.gg/safeflow"))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onActivateDeviceAdmin: () -> Unit,
    onOpenDiscord: () -> Unit,
    isAccessibilityEnabled: () -> Boolean,
    isDeviceAdminActive: () -> Boolean
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 4
    
    // Recomposition trigger when returning from settings
    var refreshTrigger by remember { mutableStateOf(0) }
    
    LaunchedEffect(refreshTrigger) {
        // Force recheck permissions
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicator
            PageIndicator(
                currentPage = currentPage,
                totalPages = totalPages
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Content based on page
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    0 -> Page1Welcome(
                        onNext = { currentPage = 1 }
                    )
                    1 -> Page2Accessibility(
                        onNext = { currentPage = 2 },
                        onOpenSettings = {
                            onOpenAccessibilitySettings()
                            refreshTrigger++
                        },
                        isEnabled = isAccessibilityEnabled()
                    )
                    2 -> Page3DeviceAdmin(
                        onNext = { currentPage = 3 },
                        onActivate = {
                            onActivateDeviceAdmin()
                            refreshTrigger++
                        },
                        isActive = isDeviceAdminActive()
                    )
                    3 -> Page4Community(
                        onFinish = onFinish,
                        onOpenDiscord = onOpenDiscord
                    )
                }
            }
        }
    }
}

@Composable
fun PageIndicator(currentPage: Int, totalPages: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 12.dp else 8.dp)
                    .then(
                        if (index == currentPage) {
                            Modifier
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = if (index == currentPage) SoftBlue else SoftGray.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }
        }
    }
}

@Composable
fun Page1Welcome(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SafeFlow",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = SoftBlue
            )

            Spacer(modifier = Modifier.height(40.dp))

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Shield",
                tint = SoftBlue,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Protégez votre navigation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bloquez automatiquement les sites indésirables et restez concentré.",
                fontSize = 16.sp,
                color = SoftGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftBlue,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "COMMENCER",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Page2Accessibility(
    onNext: () -> Unit,
    onOpenSettings: () -> Unit,
    isEnabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Accessibility,
                contentDescription = "Accessibility",
                tint = if (isEnabled) SoftBlue else SoftGray,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Permission Accessibilité",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SafeFlow a besoin de la permission d'accessibilité pour détecter les sites bloqués.\n\nVos données restent privées et locales.",
                fontSize = 16.sp,
                color = SoftGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                    Text(
                        text = "OUVRIR PARAMÈTRES",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onNext,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftBlue,
                    contentColor = Color.White,
                    disabledContainerColor = SoftGray.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isEnabled) 4.dp else 0.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SUIVANT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isEnabled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Enabled"
                        )
                    }
                }
            }

            if (!isEnabled) {
                Text(
                    text = "⚠️ Activez la permission pour continuer",
                    fontSize = 14.sp,
                    color = Color(0xFFFF5722),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun Page3DeviceAdmin(
    onNext: () -> Unit,
    onActivate: () -> Unit,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = if (isActive) SoftBlue else SoftGray,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Protection Anti-Désinstallation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Activez la protection pour empêcher la désinstallation par erreur.\n\nVous pourrez désactiver cette protection plus tard avec un code.",
                fontSize = 16.sp,
                color = SoftGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onActivate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security"
                    )
                    Text(
                        text = "ACTIVER PROTECTION",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onNext,
                enabled = isActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftBlue,
                    contentColor = Color.White,
                    disabledContainerColor = SoftGray.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isActive) 4.dp else 0.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SUIVANT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Enabled"
                        )
                    }
                }
            }

            if (!isActive) {
                Text(
                    text = "⚠️ Activez la protection pour continuer",
                    fontSize = 14.sp,
                    color = Color(0xFFFF5722),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun Page4Community(
    onFinish: () -> Unit,
    onOpenDiscord: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = "Community",
                tint = SoftBlue,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Rejoignez la Communauté",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rejoignez notre communauté Discord (anonyme) pour partager vos expériences et obtenir de l'aide.",
                fontSize = 16.sp,
                color = SoftGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenDiscord,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5865F2),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Discord"
                    )
                    Text(
                        text = "REJOINDRE DISCORD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "TERMINER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
