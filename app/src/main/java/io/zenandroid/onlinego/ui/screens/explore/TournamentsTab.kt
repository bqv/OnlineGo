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

@Composable
fun TournamentsTab(
    state: TournamentsState,
    boardTheme: BoardTheme,
    userId: Long,
) {
    ExploreSurface(
        title = state.subtitle,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.myTournaments.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "My Tournaments",
                        style = MaterialTheme.typography.h3,
                        color = MaterialTheme.colors.onBackground,
                    )

                    val pagerState = rememberPagerState { state.myTournaments.size }
                    HorizontalPager(
                        state = pagerState,
                    ) { page ->
                        val tournament = state.myTournaments[page]
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                val currentOffsetForPage = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                val pageOffset = currentOffsetForPage.absoluteValue

                                lerp(
                                    start = 0.25f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                ).also { scale ->
                                    scaleX = scale
                                    scaleY = scale
                                }

                                alpha = lerp(
                                    start = 0.25f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 64.dp),
                            ) {
                                item {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_tournament),
                                        contentDescription = "Tournament Icon",
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxSize(),
                                    )
                                }
                                items(/* tournament.games */emptyList<Game>()) { game: Game ->
                                    SmallGameItem(
                                        game = game,
                                        boardTheme = boardTheme,
                                        userId = userId,
                                        onAction = {},
                                    )
                                }
                            }
                        }
                    }
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = pagerState.pageCount,
                        activeColor = MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "All Tournaments",
                    style = MaterialTheme.typography.h3,
                    color = MaterialTheme.colors.onBackground,
                )

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    val tournaments = listOf(
                        state.liveTournaments,
                        state.correspondenceTournaments,
                        state.archivedTournaments,
                    ).flatten()

                    val state = rememberSelectableCalendarState(
                        confirmSelectionChange = { true },
                        initialSelectionMode = SelectionMode.Period,
                      //selectionState = rememberSaveable(
                      //    saver = DynamicSelectionState.Saver(confirmSelectionChange),
                      //) {
                      //    DynamicSelectionState(confirmSelectionChange,
                      //        initialSelection, initialSelectionMode)
                      //},
                    )

                    SelectableCalendar(
                        calendarState = state,
                      //dayContent = { dayState ->
                      //},
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                ) {
                    /* tournaments for day */
                    items(/* tournaments */emptyList<Unit>()) { tournament ->
                        Card(
                            modifier = Modifier
                                .height(64.dp)
                        ) {
                            /* some detail */
                        }
                    }
                }
            }

            if (state.recurringTournaments.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Recurring Tournaments",
                        style = MaterialTheme.typography.h3,
                        color = MaterialTheme.colors.onBackground,
                    )

                    LazyRow(
                    ) {
                        items(state.recurringTournaments) { tournament ->
                            Box(
                                modifier = Modifier
                                    .size(128.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_tournament),
                                    contentDescription = "Tournament Icon",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize(),
                                )

                                /* tournament deets */
                                /* countdown? */
                            }
                        }
                    }
                }
            }
        }
    }
}
