package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import com.example.presentation.auth.AuthScreen
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.home.HomeScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.profile.DebtorProfileScreen
import com.example.presentation.profile.CallDispositionScreen
import com.example.presentation.security.SecurityViewModel
import com.example.presentation.security.SecurityAndComplianceSettingsScreen
import com.example.presentation.campaign.CampaignViewModel
import com.example.presentation.campaign.CampaignControlScreen
import com.example.presentation.telephony.TelephonyViewModel
import com.example.presentation.telephony.TelephonyInfrastructureSettings
import com.example.presentation.sync.DataSyncViewModel
import com.example.presentation.sync.DataSynchronizationSettings
import com.example.presentation.components.PermissionGateway
import com.example.presentation.dashboard.AgentDashboardViewModel
import com.example.presentation.dashboard.AgentDashboardScreen
import com.example.presentation.dashboard.AdminAnalyticsDashboard
import com.example.presentation.dashboard.TelecallerTargetScreen
import com.example.presentation.dashboard.UploadDatabaseScreen
import com.example.presentation.queue.SmartCallingQueueViewModel
import com.example.presentation.queue.SmartCallingQueueScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.data.security.AppSecurityGuard
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val securityViewModel: SecurityViewModel by viewModels()
    private val campaignViewModel: CampaignViewModel by viewModels()
    private val telephonyViewModel: TelephonyViewModel by viewModels()
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val agentDashboardViewModel: AgentDashboardViewModel by viewModels()
    private val smartQueueViewModel: SmartCallingQueueViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by securityViewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionGateway(
                        onGranted = {
                            // Operational suite authorized
                        }
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current

                        var isBypassed by remember { mutableStateOf(com.example.BuildConfig.DEBUG) }
                        var isRooted by remember { mutableStateOf(false) }
                        var isVpnActive by remember { mutableStateOf(true) }
                        var isLoadingSecurityCheck by remember { mutableStateOf(true) }
                        var securityTriggerCheck by remember { mutableStateOf(0) }

                        LaunchedEffect(securityTriggerCheck) {
                            isLoadingSecurityCheck = true
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                val rooted = AppSecurityGuard.isDeviceRooted(context)
                                val vpn = AppSecurityGuard.isCorporateVpnActive(context)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    isRooted = rooted
                                    isVpnActive = vpn
                                    isLoadingSecurityCheck = false
                                }
                            }
                        }

                        val failsCompliance = !isLoadingSecurityCheck && (isRooted || !isVpnActive) && !isBypassed

                        if (failsCompliance) {
                            AlertDialog(
                                onDismissRequest = { /* Un-dismissible unless bypassed */ },
                                properties = androidx.compose.ui.window.DialogProperties(
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                ),
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Compliance Violation",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "This device does not meet the security requirements for the Dues Recovery compliance profile.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (isRooted) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "⚠️ Compromised OS: Device is rooted / EMULATOR",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }
                                        if (!isVpnActive) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "🔒 Access Denied: Corporate VPN not connected",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Please resolve these compliance issues and retry verification.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { securityTriggerCheck++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Retry Verification")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { isBypassed = true }
                                    ) {
                                        Text("Developer Bypass", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        }

                        LaunchedEffect(Unit) {
                            dataSyncViewModel.syncEvents.collect { message ->
                                val isMajorEvent = message.startsWith("SUCCESS") || 
                                                 message.startsWith("FAILURE") || 
                                                 message.startsWith("Sync Complete") || 
                                                 message.startsWith("Sync Failed")
                                if (isMajorEvent) {
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                        val selectedDebtor by homeViewModel.selectedDebtor.collectAsState()
                        val dispositionDebtor by homeViewModel.dispositionDebtor.collectAsState()

                        val isScreenshotBlockEnabled by securityViewModel.isScreenshotBlockEnabled.collectAsState()
                        LaunchedEffect(isScreenshotBlockEnabled) {
                            if (isScreenshotBlockEnabled) {
                                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                            } else {
                                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                            }
                        }

                        var showSecuritySettings by remember { mutableStateOf(false) }
                        var showCampaignControl by remember { mutableStateOf(false) }
                        var showTelephonySettings by remember { mutableStateOf(false) }
                        var showDataSyncSettings by remember { mutableStateOf(false) }
                        var showAgentDashboard by remember { mutableStateOf(false) }
                        var showSmartQueue by remember { mutableStateOf(false) }
                        var showAdminAnalytics by remember { mutableStateOf(false) }
                        var showTelecallerTargetScreen by remember { mutableStateOf(false) }
                        var showUploadDatabaseScreen by remember { mutableStateOf(false) }

                        val activeImpersonatedAgent by securityViewModel.activeImpersonatedAgent.collectAsState()

                        LaunchedEffect(activeImpersonatedAgent) {
                            if (activeImpersonatedAgent != null && showAdminAnalytics) {
                                showAdminAnalytics = false
                                showAgentDashboard = true
                            }
                        }

                        if (!isLoggedIn) {
                            AuthScreen(
                                viewModel = authViewModel,
                                onAuthSuccess = {}
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (activeImpersonatedAgent != null) {
                                    Surface(
                                        color = Color(0xFFDC2626), // High warning red
                                        contentColor = Color.White,
                                        modifier = Modifier.fillMaxWidth().testTag("admin_impersonation_banner")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "ADMIN SIMULATION MODE: Impersonating ${activeImpersonatedAgent?.name} (${activeImpersonatedAgent?.id})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "📊 Exit Simulation",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF991B1B))
                                                    .clickable { securityViewModel.setImpersonatedAgent(null) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    if (selectedDebtor != null) {
                                        DebtorProfileScreen(
                                            viewModel = homeViewModel,
                                            onBack = { homeViewModel.selectDebtor(null) }
                                        )
                                    } else {
                                        HomeScreen(
                                            viewModel = homeViewModel,
                                            onLogout = { authViewModel.logout() },
                                            onOpenSecuritySettings = { showSecuritySettings = true },
                                            onOpenCampaignControl = { showCampaignControl = true },
                                            onOpenTelephonySettings = { showTelephonySettings = true },
                                            onOpenSyncSettings = { showDataSyncSettings = true },
                                            onOpenAgentDashboard = { showAgentDashboard = true },
                                            onOpenSmartQueue = { showSmartQueue = true },
                                            onOpenAdminAnalytics = { showAdminAnalytics = true },
                                             onOpenTelecallerTargetScreen = { showTelecallerTargetScreen = true },
                                             onOpenUploadDatabaseScreen = { showUploadDatabaseScreen = true }
                                        )
                                    }

                                    if (dispositionDebtor != null) {
                                        CallDispositionScreen(
                                            debtor = dispositionDebtor!!,
                                            viewModel = homeViewModel,
                                            onDismiss = { homeViewModel.selectDispositionDebtor(null) }
                                        )
                                    }

                                    if (showSecuritySettings) {
                                        SecurityAndComplianceSettingsScreen(
                                            viewModel = securityViewModel,
                                            onBack = { showSecuritySettings = false }
                                        )
                                    }

                                    if (showCampaignControl) {
                                        CampaignControlScreen(
                                            viewModel = campaignViewModel,
                                            onBack = { showCampaignControl = false }
                                        )
                                    }

                                    if (showTelephonySettings) {
                                        TelephonyInfrastructureSettings(
                                            viewModel = telephonyViewModel,
                                            onBack = { showTelephonySettings = false }
                                        )
                                    }

                                    if (showDataSyncSettings) {
                                        DataSynchronizationSettings(
                                            viewModel = dataSyncViewModel,
                                            onBack = { showDataSyncSettings = false }
                                        )
                                    }

                                    if (showAgentDashboard) {
                                        AgentDashboardScreen(
                                            viewModel = agentDashboardViewModel,
                                            onBack = { showAgentDashboard = false },
                                            onCallDebtor = { debtor ->
                                                homeViewModel.initiateCall(debtor)
                                                showAgentDashboard = false
                                            }
                                        )
                                    }

                                    if (showSmartQueue) {
                                        SmartCallingQueueScreen(
                                            viewModel = smartQueueViewModel,
                                            onBack = { showSmartQueue = false },
                                            onCallDebtor = { debtor ->
                                                homeViewModel.initiateCall(debtor)
                                                showSmartQueue = false
                                            }
                                        )
                                    }

                                    if (showAdminAnalytics) {
                                        AdminAnalyticsDashboard(
                                            viewModel = securityViewModel,
                                            homeViewModel = homeViewModel,
                                            onBack = { showAdminAnalytics = false }
                                         )
                                     }

                                     if (showTelecallerTargetScreen) {
                                         TelecallerTargetScreen(
                                             viewModel = securityViewModel,
                                             onBack = { showTelecallerTargetScreen = false }
                                         )
                                     }

                                     if (showUploadDatabaseScreen) {
                                         UploadDatabaseScreen(
                                             homeViewModel = homeViewModel,
                                             onBack = { showUploadDatabaseScreen = false }
                                         )
                                     }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
