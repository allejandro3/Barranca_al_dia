package com.example.reportesciudadano

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reportesciudadano.data.ReportsRepository
import com.example.reportesciudadano.model.Report
import com.example.reportesciudadano.model.SimulatedData
import com.example.reportesciudadano.ui.components.AppView
import com.example.reportesciudadano.ui.components.BottomNavBar
import com.example.reportesciudadano.ui.components.FeedMode
import com.example.reportesciudadano.ui.components.MapLibreMapView
import com.example.reportesciudadano.ui.components.NavDrawer
import com.example.reportesciudadano.ui.components.NewReportForm
import com.example.reportesciudadano.ui.components.ToastBanner
import com.example.reportesciudadano.ui.components.TopSearchBar
import com.example.reportesciudadano.ui.screens.FeedView
import com.example.reportesciudadano.ui.screens.LoginScreen
import com.example.reportesciudadano.ui.screens.NotifView
import com.example.reportesciudadano.ui.screens.ProfileView
import com.example.reportesciudadano.ui.screens.RegisterScreen
import com.example.reportesciudadano.ui.screens.ReportDetailScreen
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.ReportesCiudadanoTheme

enum class Screen {
    LOGIN, REGISTER, MAIN
}

private val viewOrder = listOf(
    AppView.MAP,
    AppView.HOME,
    AppView.NOTIF,
    AppView.USER
)

class MainActivity : ComponentActivity() {
    private lateinit var repository: ReportsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = ReportsRepository(this)

        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 102)
        }

        setContent {
            ReportesCiudadanoTheme {
                MainApp(repository = repository)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(context: Context): Pair<Double, Double> {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            return Pair(location.latitude, location.longitude)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Pair(SimulatedData.FALLBACK_LAT, SimulatedData.FALLBACK_LNG)
}

@Composable
fun MainApp(repository: ReportsRepository) {
    val savedSession = remember { repository.getLoggedInSession() }
    var screen by remember { mutableStateOf(if (savedSession != null) Screen.MAIN else Screen.LOGIN) }
    var userName by remember { mutableStateOf(savedSession?.second ?: "Usuario") }
    var currentView by remember { mutableStateOf(AppView.MAP) }
    var feedMode by remember { mutableStateOf(FeedMode.INSTA) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isFormOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var pickedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedReportDetail by remember { mutableStateOf<Report?>(null) }

    // Draft form state persistence across map picking
    var draftTitle by remember { mutableStateOf("") }
    var draftCategory by remember { mutableStateOf(SimulatedData.CATEGORIES[0]) }
    var draftDescription by remember { mutableStateOf("") }
    var draftPhoto by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var userLocation by remember { mutableStateOf(Pair(SimulatedData.FALLBACK_LAT, SimulatedData.FALLBACK_LNG)) }
    var reportsState by remember { mutableStateOf(repository.reports) }

    LaunchedEffect(Unit) {
        val loc = fetchCurrentLocation(context)
        userLocation = loc
        repository.checkNearbyReports(loc.first, loc.second)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen)
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (slideInHorizontally(
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) { width -> width } + fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        ) { width -> -width } + fadeOut(tween(200)))
            },
            label = "screen_transition"
        ) { targetScreen ->
            when (targetScreen) {
                Screen.LOGIN -> {
                    LoginScreen(
                        onLoginAttempt = { email, pass, keepSession ->
                            val user = repository.loginUser(email, pass)
                            if (user != null) {
                                if (keepSession) {
                                    repository.saveSession(user.email, user.name)
                                } else {
                                    repository.clearSession()
                                }
                                userName = user.name
                                screen = Screen.MAIN
                                currentView = AppView.MAP
                                toastMessage = "¡Bienvenido/a, ${user.name}!"
                                null
                            } else {
                                "Correo o contraseña incorrectos."
                            }
                        },
                        onRecoverPassword = { email ->
                            repository.recoverPassword(email)
                        },
                        onGoToRegister = {
                            screen = Screen.REGISTER
                        }
                    )
                }

                Screen.REGISTER -> {
                    RegisterScreen(
                        onRegisterAttempt = { name, email, pass ->
                            val success = repository.registerUser(name, email, pass)
                            if (success) {
                                userName = name
                                screen = Screen.MAIN
                                currentView = AppView.MAP
                                toastMessage = "¡Cuenta creada con éxito, $name!"
                                null
                            } else {
                                "El correo electrónico ya está registrado."
                            }
                        },
                        onBackToLogin = {
                            screen = Screen.LOGIN
                        }
                    )
                }

                Screen.MAIN -> {
                    Scaffold(
                        topBar = {
                            TopSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it }
                            )
                        },
                        bottomBar = {
                            BottomNavBar(
                                currentView = currentView,
                                feedMode = feedMode,
                                onMenuClick = { isDrawerOpen = true },
                                onViewSelect = { view ->
                                    selectedReportDetail = null
                                    currentView = view
                                },
                                onToggleFeedMode = {
                                    feedMode = if (feedMode == FeedMode.INSTA) FeedMode.PIN else FeedMode.INSTA
                                    val msg = if (feedMode == FeedMode.INSTA) "Vista tipo publicación" else "Vista tipo Pinterest"
                                    toastMessage = msg
                                }
                            )
                        },
                        containerColor = DarkGreen
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentView,
                                transitionSpec = {
                                    val forward = viewOrder.indexOf(targetState) >=
                                            viewOrder.indexOf(initialState)
                                    val direction = if (forward) 1 else -1
                                    (slideInHorizontally(
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) { width -> width / 4 * direction } +
                                            fadeIn(tween(220)) +
                                            scaleIn(
                                                initialScale = 0.97f,
                                                animationSpec = tween(300)
                                            )) togetherWith
                                            (slideOutHorizontally(
                                                animationSpec = tween(
                                                    220,
                                                    easing = FastOutSlowInEasing
                                                )
                                            ) { width -> -width / 4 * direction } +
                                                    fadeOut(tween(160)))
                                },
                                label = "view_transition"
                            ) { targetView ->
                                when (targetView) {
                                    AppView.MAP -> {
                                        AnimatedContent(
                                            targetState = selectedReportDetail,
                                            transitionSpec = {
                                                val forward = targetState != null
                                                val direction = if (forward) 1 else -1
                                                (slideInHorizontally(
                                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                                ) { width -> width / 4 * direction } +
                                                        fadeIn(tween(220)) +
                                                        scaleIn(
                                                            initialScale = 0.97f,
                                                            animationSpec = tween(300)
                                                        )) togetherWith
                                                        (slideOutHorizontally(
                                                            animationSpec = tween(
                                                                220,
                                                                easing = FastOutSlowInEasing
                                                            )
                                                        ) { width -> -width / 4 * direction } +
                                                                fadeOut(tween(160)))
                                            },
                                            label = "detail_transition_map"
                                        ) { detailReport ->
                                            if (detailReport != null) {
                                                ReportDetailScreen(
                                                    report = detailReport,
                                                    onBack = { selectedReportDetail = null },
                                                    onSupportClick = { id ->
                                                        repository.toggleSupport(id)
                                                        reportsState = repository.reports
                                                    },
                                                    onDislikeClick = { id ->
                                                        repository.toggleDislike(id)
                                                        reportsState = repository.reports
                                                    }
                                                )
                                            } else {
                                                MapLibreMapView(
                                                    reports = reportsState,
                                                    userLocation = userLocation,
                                                    pickedLocation = pickedLocation,
                                                    onPickLocation = { loc -> pickedLocation = loc },
                                                    onReportHereClick = { loc ->
                                                        pickedLocation = loc
                                                        isFormOpen = true
                                                    },
                                                    onReportClick = { report ->
                                                        selectedReportDetail = report
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    AppView.HOME -> {
                                        AnimatedContent(
                                            targetState = selectedReportDetail,
                                            transitionSpec = {
                                                val forward = targetState != null
                                                val direction = if (forward) 1 else -1
                                                (slideInHorizontally(
                                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                                ) { width -> width / 4 * direction } +
                                                        fadeIn(tween(220)) +
                                                        scaleIn(
                                                            initialScale = 0.97f,
                                                            animationSpec = tween(300)
                                                        )) togetherWith
                                                        (slideOutHorizontally(
                                                            animationSpec = tween(
                                                                220,
                                                                easing = FastOutSlowInEasing
                                                            )
                                                        ) { width -> -width / 4 * direction } +
                                                                fadeOut(tween(160)))
                                            },
                                            label = "detail_transition_home"
                                        ) { detailReport ->
                                            if (detailReport != null) {
                                                ReportDetailScreen(
                                                    report = detailReport,
                                                    onBack = { selectedReportDetail = null },
                                                    onSupportClick = { id ->
                                                        repository.toggleSupport(id)
                                                        reportsState = repository.reports
                                                    },
                                                    onDislikeClick = { id ->
                                                        repository.toggleDislike(id)
                                                        reportsState = repository.reports
                                                    }
                                                )
                                            } else {
                                                FeedView(
                                                    reports = reportsState,
                                                    searchQuery = searchQuery,
                                                    mode = feedMode,
                                                    onSupportClick = { id ->
                                                        repository.toggleSupport(id)
                                                        reportsState = repository.reports
                                                    },
                                                    onDislikeClick = { id ->
                                                        repository.toggleDislike(id)
                                                        reportsState = repository.reports
                                                    },
                                                    loadComments = { id ->
                                                        repository.getComments(id)
                                                    },
                                                    onAddComment = { id, text ->
                                                        repository.addComment(id, userName, text)
                                                    },
                                                    onReportClick = { report ->
                                                        selectedReportDetail = report
                                                    },
                                                    onOpenFormClick = { isFormOpen = true }
                                                )
                                            }
                                        }
                                    }

                                    AppView.NOTIF -> {
                                        AnimatedContent(
                                            targetState = selectedReportDetail,
                                            transitionSpec = {
                                                val forward = targetState != null
                                                val direction = if (forward) 1 else -1
                                                (slideInHorizontally(
                                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                                ) { width -> width / 4 * direction } +
                                                        fadeIn(tween(220)) +
                                                        scaleIn(
                                                            initialScale = 0.97f,
                                                            animationSpec = tween(300)
                                                        )) togetherWith
                                                        (slideOutHorizontally(
                                                            animationSpec = tween(
                                                                220,
                                                                easing = FastOutSlowInEasing
                                                            )
                                                        ) { width -> -width / 4 * direction } +
                                                                fadeOut(tween(160)))
                                            },
                                            label = "detail_transition_notif"
                                        ) { detailReport ->
                                            if (detailReport != null) {
                                                ReportDetailScreen(
                                                    report = detailReport,
                                                    onBack = { selectedReportDetail = null },
                                                    onSupportClick = { id ->
                                                        repository.toggleSupport(id)
                                                        reportsState = repository.reports
                                                    },
                                                    onDislikeClick = { id ->
                                                        repository.toggleDislike(id)
                                                        reportsState = repository.reports
                                                    }
                                                )
                                            } else {
                                                NotifView(
                                                    repository = repository,
                                                    onNotificationClick = { report ->
                                                        selectedReportDetail = report
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    AppView.USER -> {
                                        AnimatedContent(
                                            targetState = selectedReportDetail,
                                            transitionSpec = {
                                                val forward = targetState != null
                                                val direction = if (forward) 1 else -1
                                                (slideInHorizontally(
                                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                                ) { width -> width / 4 * direction } +
                                                        fadeIn(tween(220)) +
                                                        scaleIn(
                                                            initialScale = 0.97f,
                                                            animationSpec = tween(300)
                                                        )) togetherWith
                                                        (slideOutHorizontally(
                                                            animationSpec = tween(
                                                                220,
                                                                easing = FastOutSlowInEasing
                                                            )
                                                        ) { width -> -width / 4 * direction } +
                                                                fadeOut(tween(160)))
                                            },
                                            label = "detail_transition_user"
                                        ) { detailReport ->
                                            if (detailReport != null) {
                                                ReportDetailScreen(
                                                    report = detailReport,
                                                    onBack = { selectedReportDetail = null },
                                                    onSupportClick = { id ->
                                                        repository.toggleSupport(id)
                                                        reportsState = repository.reports
                                                    },
                                                    onDislikeClick = { id ->
                                                        repository.toggleDislike(id)
                                                        reportsState = repository.reports
                                                    }
                                                )
                                            } else {
                                                ProfileView(
                                                    userName = userName,
                                                    reports = reportsState,
                                                    onLogoutClick = {
                                                        repository.clearSession()
                                                        screen = Screen.LOGIN
                                                    },
                                                    onNewReportClick = {
                                                        isFormOpen = true
                                                    },
                                                    onReportClick = { report ->
                                                        selectedReportDetail = report
                                                    }
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

        // Side Navigation Drawer Overlay
        NavDrawer(
            isOpen = isDrawerOpen,
            currentView = currentView,
            onClose = { isDrawerOpen = false },
            onNavigate = { view ->
                selectedReportDetail = null
                currentView = view
                if (view == AppView.HOME) {
                    feedMode = FeedMode.INSTA
                    toastMessage = "Vista tipo publicación"
                }
            }
        )

        // New Report Form Overlay with persisted draft state
        NewReportForm(
            isOpen = isFormOpen,
            onClose = { isFormOpen = false },
            userLocation = userLocation,
            pickedLocation = pickedLocation,
            userName = userName,
            draftTitle = draftTitle,
            onDraftTitleChange = { draftTitle = it },
            draftCategory = draftCategory,
            onDraftCategoryChange = { draftCategory = it },
            draftDescription = draftDescription,
            onDraftDescriptionChange = { draftDescription = it },
            draftPhoto = draftPhoto,
            onDraftPhotoChange = { draftPhoto = it },
            onSubmitReport = { newReport ->
                repository.addReport(newReport)
                reportsState = repository.reports
                isFormOpen = false
                pickedLocation = null
                draftTitle = ""
                draftDescription = ""
                draftPhoto = null
                currentView = AppView.USER
                toastMessage = "Reporte enviado"
            },
            onPickOnMap = {
                isFormOpen = false
                currentView = AppView.MAP
                toastMessage = "Toca el mapa o arrastra el pin y pulsa “Reportar aquí”"
            }
        )

        // Top Toast Badge
        ToastBanner(
            message = toastMessage,
            onDismiss = { toastMessage = null }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    ReportesCiudadanoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGreen)
        ) {
            LoginScreen(
                onLoginAttempt = { _, _, _ -> null },
                onRecoverPassword = { _ -> "123456" },
                onGoToRegister = {}
            )
        }
    }
}
