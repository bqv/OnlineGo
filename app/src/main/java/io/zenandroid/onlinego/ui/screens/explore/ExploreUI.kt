package io.zenandroid.onlinego.ui.screens.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.HorizontalPagerIndicator
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.BoardTheme
import io.zenandroid.onlinego.data.model.local.Game
import io.zenandroid.onlinego.gamelogic.Util
import io.zenandroid.onlinego.ui.screens.explore.ExploreState
//import io.zenandroid.onlinego.ui.screens.explore.composables.*
import io.zenandroid.onlinego.ui.screens.mygames.composables.SmallGameItem
import io.zenandroid.onlinego.utils.egfToRank
import io.zenandroid.onlinego.utils.formatRank
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreState,
    openLadder: (Long) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var currentTab by remember { mutableStateOf(0) }

    val tabs = ExploreTab.values().map { it.name.capitalize() }

    Scaffold(
        topBar = {
            Column {
                var headerHeight by remember { mutableStateOf(0.dp) }

                if (state.title == null) {
                    val density = LocalDensity.current

                    TabRow(
                        selectedTabIndex = currentTab,
                        modifier = Modifier.onGloballyPositioned {
                            headerHeight = with(density) {
                                it.size.height.toDp()
                            }
                        },
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = {
                                    Text(
                                        text = title,
                                    )
                                },
                                icon = {
                                    val drawable = when (ExploreTab[index]!!) {
                                        ExploreTab.LADDERS -> R.drawable.ic_ladder
                                        ExploreTab.TOURNAMENTS -> R.drawable.ic_tournament
                                        ExploreTab.GROUPS -> R.drawable.ic_group
                                    }

                                    Icon(
                                        painter = painterResource(drawable),
                                        contentDescription = title,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(24.dp),
                                    )
                                },
                                selected = currentTab == index,
                                onClick = { currentTab = index },
                            )
                        }
                    }
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = state.title,
                                style = MaterialTheme.typography.h1,
                                color = MaterialTheme.colors.onSurface,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { onBack() }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                            }
                        },
                        backgroundColor = MaterialTheme.colors.surface,
                        modifier = Modifier.height(headerHeight),
                    )

                    BackHandler { onBack() }
                }
                if (state.loading) {
                    LinearProgressIndicator(
                        color = MaterialTheme.colors.primary,
                        trackColor = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Material3Theme.colorScheme.surface,
                contentColor = Material3Theme.colorScheme.onSurface,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Hmm.",
                )
            }

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openLadder(0) }
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
        },
    ) { innerPadding ->
        Column (
            modifier = Modifier.fillMaxSize(),
        ) {
            when (ExploreTab[currentTab]!!) {
                ExploreTab.LADDERS -> LaddersTab(
                    state = state.laddersState,
                )
                ExploreTab.TOURNAMENTS -> TournamentsTab(
                    state = state.tournamentsState,
                    boardTheme = state.boardTheme,
                    userId = state.playerDetails?.id!!,
                )
                ExploreTab.GROUPS -> GroupsTab(
                    state = state.groupsState,
                )
            }
        }
    }
}

@Composable
fun ExploreSurface(
    title: String,
    content: @Composable () -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h2,
                color = MaterialTheme.colors.onBackground,
            )
            content()
        }
    }
}
