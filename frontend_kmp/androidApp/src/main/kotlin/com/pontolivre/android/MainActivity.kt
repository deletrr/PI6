package com.pontolivre.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.zxing.integration.android.IntentIntegrator
import com.pontolivre.shared.api.initTokenStorage
import com.pontolivre.shared.repository.AppSession
import com.pontolivre.shared.ui.navigation.Screen
import com.pontolivre.shared.ui.screens.admin.*
import com.pontolivre.shared.ui.screens.user.*
import com.pontolivre.shared.ui.theme.PontoLivreTheme
import com.pontolivre.shared.viewmodel.*

class MainActivity : ComponentActivity() {

    private var onQrResult: ((String) -> Unit)? = null

    private val qrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            ?.contents
            ?.let { onQrResult?.invoke(it) }
        onQrResult = null
    }

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchQrScanner() }

    private fun launchQrScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Aponte para o QR Code da vaga")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        qrLauncher.launch(integrator.createScanIntent())
    }

    fun requestQrScan(onResult: (String) -> Unit) {
        onQrResult = onResult
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Smart Parking Notifications"
            val descriptionText = "Alertas de tempo de estacionamento"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("PARKING_ALERTS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "PARKING_ALERTS")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // Permissão não concedida
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        initTokenStorage(applicationContext)
        AppSession.init()
        setContent {
            PontoLivreTheme {
                PontoLivreApp(onShowNotification = { t, m -> showNotification(t, m) })
            }
        }
    }
}

@Composable
fun PontoLivreApp(onShowNotification: (String, String) -> Unit) {
    val navController = rememberNavController()

    val startDestination = remember {
        if (AppSession.isLoggedIn)
            if (AppSession.isAdmin) Screen.AdminDashboard.route else Screen.Home.route
        else Screen.Login.route
    }

    // Shared ViewModels
    val sessionVm       = remember { SessionViewModel() }
    val walletVm        = remember { WalletViewModel() }
    val vehicleVm       = remember { VehicleViewModel() }
    val adminMetersVm   = remember { AdminMetersViewModel() }
    val adminSessionsVm = remember { AdminSessionsViewModel() }
    val adminLogsVm     = remember { AdminLogsViewModel() }

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            val vm = remember { AuthViewModel() }
            LoginScreen(vm,
                onLoginSuccess = {
                    val dest = if (AppSession.isAdmin) Screen.AdminDashboard.route
                               else Screen.Home.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) })
        }

        composable(Screen.Register.route) {
            val vm = remember { AuthViewModel() }
            RegisterScreen(vm,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() })
        }

        // ── User ──────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            val homeVm = remember { HomeViewModel() }
            UserBottomNav(navController) {
                HomeScreen(homeVm,
                    onNavigateToActiveSession = { navController.navigate(Screen.ActiveSession.route) },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet.route) },
                    onNavigateToMeterDetail = { id ->
                        if (id == "history") navController.navigate(Screen.History.route)
                        else navController.navigate(Screen.MeterDetail.createRoute(id))
                    },
                    onNavigateToVehicles = {
                        navController.navigate(Screen.Vehicles.route)
                    },
                    onNavigateToClaimSession = { code ->
                        navController.navigate(Screen.VehicleSelection.createRoute(code))
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    })
            }
        }

        composable(Screen.MeterDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("meterId") ?: ""
            MeterDetailScreen(id,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.ActiveSession.route) {
            ActiveSessionScreen(sessionVm,
                onShowNotification = onShowNotification,
                onNavigateUp   = { navController.popBackStack() },
                onSessionEnded = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                })
        }

        composable(Screen.VehicleSelection.route) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            VehicleSelectionScreen(code, sessionVm, vehicleVm,
                onSuccess = {
                    navController.navigate(Screen.ActiveSession.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.Vehicles.route) {
            UserBottomNav(navController) {
                VehiclesScreen(vehicleVm, onNavigateUp = { navController.popBackStack() })
            }
        }

        composable(Screen.Wallet.route) {
            UserBottomNav(navController) {
                WalletScreen(walletVm,
                    onNavigateUp        = { navController.popBackStack() },
                    onNavigateToRecharge = { navController.navigate(Screen.Recharge.route) },
                    onNavigateToExtract  = { navController.navigate(Screen.Extract.route) },
                    onNavigateToTransactionDetail = { id -> 
                        navController.navigate(Screen.TransactionDetail.createRoute(id))
                    })
            }
        }

        composable(Screen.Recharge.route) {
            RechargeScreen(walletVm, onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.Extract.route) {
            ExtractScreen(walletVm, 
                onNavigateUp = { navController.popBackStack() },
                onNavigateToTransactionDetail = { id ->
                    navController.navigate(Screen.TransactionDetail.createRoute(id))
                })
        }

        composable(Screen.TransactionDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            TransactionDetailScreen(id, walletVm, onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.Fines.route) {
            val vm = remember { FinesViewModel() }
            UserBottomNav(navController) {
                FinesScreen(vm, onNavigateUp = { navController.popBackStack() })
            }
        }

        composable(Screen.Support.route) {
            val vm = remember { SupportViewModel() }
            UserBottomNav(navController) {
                SupportScreen(vm, onNavigateUp = { navController.popBackStack() })
            }
        }

        composable(Screen.Meters.route) {
            val homeVm = remember { HomeViewModel() }
            UserBottomNav(navController) {
                MetersScreen(homeVm,
                    onNavigateToMeterDetail = { id ->
                        navController.navigate(Screen.MeterDetail.createRoute(id))
                    },
                    onNavigateUp = { navController.popBackStack() })
            }
        }

        composable(Screen.Profile.route) {
            UserBottomNav(navController) {
                ProfileScreen(
                    onNavigateToFines = { navController.navigate(Screen.Fines.route) },
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                    onNavigateUp = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    })
            }
        }

        // ── Admin ─────────────────────────────────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            val vm = remember { AdminDashboardViewModel() }
            AdminDashboardScreen(vm,
                onNavigate = { navController.navigate(it) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }

        composable(Screen.AdminUsers.route) {
            AdminUsersScreen(remember { AdminUsersViewModel() },
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminMeters.route) {
            AdminMetersScreen(adminMetersVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminOrphans.route) {
            AdminOrphansScreen(adminMetersVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminSessions.route) {
            AdminSessionsScreen(adminSessionsVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminActiveSessions.route) {
            AdminActiveSessionsScreen(adminSessionsVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminOccupiedMeters.route) {
            AdminOccupiedMetersScreen(adminMetersVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminObstructed.route) {
            AdminObstructedScreen(adminMetersVm, adminSessionsVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminFines.route) {
            AdminFinesScreen(remember { AdminFinesViewModel() },
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminSupport.route) {
            AdminSupportScreen(remember { AdminSupportViewModel() },
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminExtract.route) {
            AdminExtractScreen(walletVm,
                onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.AdminLogs.route) {
            AdminLogsScreen(adminLogsVm,
                onNavigateUp = { navController.popBackStack() })
        }
    }
}

@Composable
fun UserBottomNav(navController: NavController, content: @Composable () -> Unit) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) }, label = { Text("Início") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    })
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalParking, null) }, label = { Text("Vagas") },
                    selected = currentRoute == Screen.Meters.route,
                    onClick = { navController.navigate(Screen.Meters.route) })
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, null) }, label = { Text("Carteira") },
                    selected = currentRoute == Screen.Wallet.route,
                    onClick = { navController.navigate(Screen.Wallet.route) })
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") },
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navController.navigate(Screen.Profile.route) })
            }
        }
    ) { padding -> Box(Modifier.padding(padding)) { content() } }
}
