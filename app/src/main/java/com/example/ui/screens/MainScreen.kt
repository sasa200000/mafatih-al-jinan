package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MafatihDataSource
import com.example.model.BorderLightSettings
import com.example.model.BorderLightShape
import com.example.model.PrayerItem
import com.example.ui.components.ScreenEdgeLighting
import com.example.ui.components.SupportBottomSheet
import com.example.ui.components.TasbihView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.utils.rememberPrayerReciter
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TurquoiseNeon

enum class AppTab(val titleFa: String) {
    MAFATIH("مفاتیح"),
    LIGHT_SHOW("رقص نور"),
    TASBIH("ذکرشمار"),
    FAVORITES("نشان‌شده‌ها")
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mafatih_prefs", Context.MODE_PRIVATE) }

    val initialFavorites = remember {
        prefs.getStringSet("favorite_ids", setOf("ziyarat_ashura", "dua_kumayl", "dua_faraj")) ?: emptySet()
    }

    var favorites by remember { mutableStateOf(initialFavorites) }
    var currentTab by remember { mutableStateOf(AppTab.MAFATIH) }
    var selectedPrayer by remember { mutableStateOf<PrayerItem?>(null) }
    var showSupportSheet by remember { mutableStateOf(false) }

    // Welcome recitation: play Salawat of Hazrat Fatima al-Zahra once on app entry.
    val welcomeReciter = rememberPrayerReciter()
    val mainLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(mainLifecycleOwner, welcomeReciter) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) welcomeReciter.refresh()
        }
        mainLifecycleOwner.lifecycle.addObserver(obs)
        onDispose { mainLifecycleOwner.lifecycle.removeObserver(obs) }
    }
    var welcomePlayed by remember { mutableStateOf(false) }
    LaunchedEffect(welcomeReciter.isReady) {
        if (welcomeReciter.isReady && !welcomePlayed) {
            welcomePlayed = true
            val salawat = MafatihDataSource.prayers.firstOrNull { it.id == "salawat_fatima" }
            salawat?.let { welcomeReciter.play(it.verses.map { v -> v.id to v.arabic }) }
        }
    }
    var borderLightSettings by remember {
        mutableStateOf(BorderLightSettings(isEnabled = true, isGlobalEnabled = false))
    }

    fun toggleFavorite(id: String) {
        val newFavs = if (favorites.contains(id)) {
            favorites - id
        } else {
            favorites + id
        }
        favorites = newFavs
        prefs.edit().putStringSet("favorite_ids", newFavs).apply()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                if (selectedPrayer == null) {
                    NavigationBar(
                        containerColor = EmeraldDark,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("app_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.MAFATIH,
                            onClick = { currentTab = AppTab.MAFATIH },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "مفاتیح"
                                )
                            },
                            label = { Text("مفاتیح", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldDark,
                                selectedTextColor = GoldAccent,
                                indicatorColor = GoldAccent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_mafatih")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.LIGHT_SHOW,
                            onClick = { currentTab = AppTab.LIGHT_SHOW },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "رقص نور چندشکلی"
                                )
                            },
                            label = { Text("رقص نور", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldDark,
                                selectedTextColor = TurquoiseNeon,
                                indicatorColor = TurquoiseNeon,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_light_show")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.TASBIH,
                            onClick = { currentTab = AppTab.TASBIH },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "ذکرشمار"
                                )
                            },
                            label = { Text("ذکرشمار", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldDark,
                                selectedTextColor = GoldAccent,
                                indicatorColor = GoldAccent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_tasbih")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.FAVORITES,
                            onClick = { currentTab = AppTab.FAVORITES },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "نشان‌شده‌ها"
                                )
                            },
                            label = { Text("نشان‌شده‌ها", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldDark,
                                selectedTextColor = GoldAccent,
                                indicatorColor = GoldAccent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedPrayer != null) {
                    val activePrayer = selectedPrayer!!
                    PrayerDetailScreen(
                        prayer = activePrayer,
                        isFavorite = favorites.contains(activePrayer.id),
                        onToggleFavorite = { toggleFavorite(activePrayer.id) },
                        onBack = { selectedPrayer = null },
                        onOpenSupport = { showSupportSheet = true }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabTransition"
                    ) { tab ->
                        when (tab) {
                            AppTab.MAFATIH -> {
                                MafatihListScreen(
                                    prayers = MafatihDataSource.prayers,
                                    favorites = favorites,
                                    isBorderLightActive = borderLightSettings.isEnabled && borderLightSettings.isGlobalEnabled,
                                    onToggleBorderLighting = {
                                        borderLightSettings = borderLightSettings.copy(
                                            isGlobalEnabled = !borderLightSettings.isGlobalEnabled,
                                            isEnabled = true
                                        )
                                    },
                                    onPrayerClick = { selectedPrayer = it },
                                    onToggleFavorite = { toggleFavorite(it) },
                                    onOpenSupport = { showSupportSheet = true },
                                    onOpenLightShow = { currentTab = AppTab.LIGHT_SHOW }
                                )
                            }
                            AppTab.LIGHT_SHOW -> {
                                LightShowScreen(
                                    borderSettings = borderLightSettings,
                                    onUpdateBorderSettings = { borderLightSettings = it },
                                    onOpenSupport = { showSupportSheet = true }
                                )
                            }
                            AppTab.TASBIH -> {
                                TasbihView(
                                    modifier = Modifier.fillMaxSize(),
                                    initialTarget = 100
                                )
                            }
                            AppTab.FAVORITES -> {
                                FavoritesScreen(
                                    prayers = MafatihDataSource.prayers,
                                    favorites = favorites,
                                    onPrayerClick = { selectedPrayer = it },
                                    onToggleFavorite = { toggleFavorite(it) },
                                    onOpenSupport = { showSupportSheet = true }
                                )
                            }
                        }
                    }
                }

                // Global Edge Lighting around the border of the screen (دور تا دور صفحه)
                if (borderLightSettings.isEnabled && borderLightSettings.isGlobalEnabled && currentTab != AppTab.LIGHT_SHOW) {
                    ScreenEdgeLighting(
                        settings = borderLightSettings,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Support Bottom Sheet if triggered
                if (showSupportSheet) {
                    SupportBottomSheet(
                        onDismiss = { showSupportSheet = false }
                    )
                }
            }
        }
    }
}
