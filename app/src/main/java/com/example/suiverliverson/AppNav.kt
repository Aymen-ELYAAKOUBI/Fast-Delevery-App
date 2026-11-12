package com.example.suiverliverson

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class Tab(val route: String, val icon: ImageVector) {
    HOME("home", Icons.Default.Home),
    STATS("stats", Icons.Default.BarChart),
    MAP("map", Icons.Default.Place),
    PROFILE("profile", Icons.Default.Person);

    fun label(s: Strings) = when (this) {
        HOME -> s.home
        STATS -> s.stats
        MAP -> s.map
        PROFILE -> s.profile
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val s = LocalStrings.current
    val context = LocalContext.current
    val store = remember { AccountStore(context) }
    val deliveryManager = remember { DeliveryManager() }
    var currentAccount by remember { mutableStateOf(store.current()) }
    var signupRole by remember { mutableStateOf(AccountRole.DRIVER) }

    fun destinationFor(account: Account?): String = when (account?.role) {
        AccountRole.CLIENT -> "clientHome"
        AccountRole.COMPANY -> "companyHome"
        else -> Tab.HOME.route
    }

    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val showBar = currentAccount?.role == AccountRole.DRIVER &&
            Tab.entries.any { it.route == current }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(Tab.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label(s)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "splash",
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(240)) + slideInHorizontally(tween(240)) { it / 18 } },
            exitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { -it / 24 } },
            popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 24 } },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 18 } }
        ) {
            composable("splash") {
                SplashScreen {
                    val account = store.current()
                    currentAccount = account
                    nav.navigate(if (account == null) "login" else destinationFor(account)) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            composable("login") {
                LoginScreen(
                    store = store,
                    onLogin = {
                        currentAccount = store.current()
                        nav.navigate(destinationFor(currentAccount)) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onCreateAccount = { role ->
                        signupRole = role
                        nav.navigate("signup")
                    },
                    onForgotPassword = { nav.navigate("forgot") }
                )
            }
            composable("signup") {
                SignUpScreen(
                    store = store,
                    initialRole = signupRole,
                    onBack = { nav.popBackStack() },
                    onSuccess = {
                        currentAccount = store.current()
                        nav.navigate(destinationFor(currentAccount)) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("forgot") {
                ForgotPasswordScreen(
                    store = store,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Tab.HOME.route) {
                HomeScreen(
                    deliveryManager = deliveryManager,
                    onNavigateToMap = { nav.navigate(Tab.MAP.route) },
                    onNavigateToNotifications = { nav.navigate("notifications") }
                )
            }
            composable(Tab.STATS.route) {
                StatsScreen(deliveryManager)
            }
            composable(Tab.MAP.route) {
                MapScreen(
                    deliveryManager = deliveryManager,
                    onDeliveryComplete = {
                        if (!nav.popBackStack(Tab.HOME.route, inclusive = false)) {
                            nav.navigate(Tab.HOME.route) { launchSingleTop = true }
                        }
                    }
                )
            }
            composable(Tab.PROFILE.route) {
                ProfileScreen(
                    store = store,
                    deliveryManager = deliveryManager,
                    onNavigateToSettings = { nav.navigate("settings") },
                    onNavigateToHistory = { nav.navigate("history") },
                    onLogout = {
                        store.logout()
                        currentAccount = null
                        nav.navigate("login") { popUpTo(Tab.HOME.route) { inclusive = true } }
                    }
                )
            }
            composable("clientHome") {
                currentAccount?.let { account ->
                    RoleHomeScreen(
                        account = account,
                        onLogout = {
                            store.logout()
                            currentAccount = null
                            nav.navigate("login") {
                                popUpTo("clientHome") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("companyHome") {
                currentAccount?.let { account ->
                    RoleHomeScreen(
                        account = account,
                        onLogout = {
                            store.logout()
                            currentAccount = null
                            nav.navigate("login") {
                                popUpTo("companyHome") { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable("notifications") {
                NotificationsScreen(
                    deliveryManager = deliveryManager,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    store = store,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(
                    deliveryManager = deliveryManager,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
