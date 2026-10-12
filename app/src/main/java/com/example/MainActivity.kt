package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DocumentRepository
import com.example.data.repository.EvaluationRepository
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.ResearchRepository
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.MethodologyScreen
import com.example.ui.screens.ResearchLabScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.AccessibleTeal
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TruthTraceTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class AppDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Overview", Icons.Default.Public),
    WORKSPACE("Workspace", Icons.Default.Explore),
    DOCUMENTS("Documents", Icons.Default.Description),
    LAB("Research Lab", Icons.Default.Science),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val documentRepository = DocumentRepository()
        val firestoreRepository = try {
            FirestoreRepository(this)
        } catch (_: Exception) {
            null
        }
        val researchRepository = ResearchRepository(documentRepository, firestoreRepository)
        val evaluationRepository = EvaluationRepository(documentRepository, firestoreRepository)

        setContent {
            TruthTraceTheme {
                TruthTraceApp(
                    documentRepository = documentRepository,
                    researchRepository = researchRepository,
                    evaluationRepository = evaluationRepository,
                    firestoreRepository = firestoreRepository
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruthTraceApp(
    documentRepository: DocumentRepository,
    researchRepository: ResearchRepository,
    evaluationRepository: EvaluationRepository,
    firestoreRepository: FirestoreRepository?
) {
    var currentDestination by remember { mutableStateOf(AppDestination.OVERVIEW) }
    var showMethodologyView by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Predictive Back navigation
    if (showMethodologyView) {
        BackHandler {
            showMethodologyView = false
        }
    } else if (currentDestination != AppDestination.OVERVIEW) {
        BackHandler {
            currentDestination = AppDestination.OVERVIEW
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    if (showMethodologyView) {
                        IconButton(onClick = { showMethodologyView = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ElectricCyan
                            )
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF133E39),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(ElectricCyan, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showMethodologyView) "Methodology" else "TruthTrace",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF133E39)
                        ) {
                            Text(
                                text = "RESEARCH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 6.dp
            ) {
                AppDestination.values().forEach { destination ->
                    val isSelected = !showMethodologyView && currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            showMethodologyView = false
                            currentDestination = destination
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFF162B3D)
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MidnightNavy)
        ) {
            if (showMethodologyView) {
                MethodologyScreen()
            } else {
                when (currentDestination) {
                    AppDestination.OVERVIEW -> LandingScreen(
                        onOpenWorkspace = { currentDestination = AppDestination.WORKSPACE },
                        onExploreMethodology = { showMethodologyView = true }
                    )
                    AppDestination.WORKSPACE -> WorkspaceScreen(
                        documentRepository = documentRepository,
                        researchRepository = researchRepository
                    )
                    AppDestination.DOCUMENTS -> DocumentsScreen(
                        documentRepository = documentRepository
                    )
                    AppDestination.LAB -> ResearchLabScreen(
                        evaluationRepository = evaluationRepository
                    )
                    AppDestination.SETTINGS -> SettingsScreen(
                        currentUser = currentUser,
                        onAuthChanged = {
                            currentUser = Firebase.auth.currentUser
                        }
                    )
                }
            }
        }
    }
}
