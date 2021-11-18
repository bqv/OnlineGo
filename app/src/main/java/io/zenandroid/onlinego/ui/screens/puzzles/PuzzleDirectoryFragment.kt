package io.zenandroid.onlinego.ui.screens.puzzle

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.accompanist.pager.*
import com.jakewharton.rxbinding2.view.RxView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.movement.MovementMethodPlugin
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.gamelogic.RulesManager
import io.zenandroid.onlinego.utils.showIf
import io.zenandroid.onlinego.ui.composables.Board
import io.zenandroid.onlinego.ui.composables.RatingBar
import io.zenandroid.onlinego.ui.screens.main.MainActivity
import io.zenandroid.onlinego.ui.screens.puzzle.PuzzleDirectoryAction.*
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme
import io.zenandroid.onlinego.data.model.StoneType
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.data.repositories.SettingsRepository
import io.zenandroid.onlinego.utils.PersistenceManager
import io.zenandroid.onlinego.utils.convertCountryCodeToEmojiFlag
import org.commonmark.node.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.Instant.now
import org.threeten.bp.temporal.ChronoUnit.*

private const val TAG = "PuzzleDirectoryFragment"

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalComposeUiApi
class PuzzleDirectoryFragment : Fragment(), MviView<PuzzleDirectoryState, PuzzleDirectoryAction> {
    private val puzzleRepository: io.zenandroid.onlinego.data.repositories.PuzzleRepository = org.koin.core.context.GlobalContext.get().get()
    private val settingsRepository: SettingsRepository by inject()
    private val viewModel: PuzzleDirectoryViewModel by viewModel()

    private val internalActions = PublishSubject.create<PuzzleDirectoryAction>()
    private var currentState: PuzzleDirectoryState? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            internalActions.onNext(UserPressedBack)
            findNavController().navigateUp()
        }
    }

    @Composable
    private fun Header(text: String) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                OnlineGoTheme {
                    val listState = rememberLazyListState()
                    LazyColumn (
                        state = listState,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        stickyHeader {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "Puzzles",
                                        fontSize = 18.sp
                                    )
                                },
                                elevation = 1.dp,
                                navigationIcon = {
                                    IconButton(onClick = { findNavController().navigateUp() }) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                                    }
                                },
                                backgroundColor = MaterialTheme.colors.surface
                            )
                        }

                        items(items = puzzleRepository.getAllPuzzleCollections().blockingFirst()) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .height(150.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(modifier = Modifier
                                        .clickable { -> navigateToPuzzleScreen(it) }) {
                                    Column(modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 10.dp)) {
                                        it.starting_puzzle.let {
                                            val pos = RulesManager.newPosition(it.width, it.height, it.initial_state)
                                            Board(
                                                boardWidth = it.width,
                                                boardHeight = it.height,
                                                position = pos,
                                                drawCoordinates = false,
                                                interactive = false,
                                                drawShadow = false,
                                                fadeInLastMove = false,
                                                fadeOutRemovedStones = false,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(MaterialTheme.shapes.small)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(modifier = Modifier.height(16.dp)
                                                .align(Alignment.CenterHorizontally)) {
                                            RatingBar(
                                                rating = it.rating,
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            val rating_count = when {
                                                it.rating_count < 1000 -> "${it.rating_count}"
                                                else -> "${it.rating_count / 1000}k"
                                            }
                                            Text(
                                                text = "($rating_count)",
                                                color = MaterialTheme.colors.onBackground,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)
                                            .padding(bottom = 8.dp, end = 4.dp)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = it.name,
                                                style = TextStyle.Default.copy(
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            val private = if(it.private) "(private)" else ""
                                            val ago = "${DAYS.between(it.created, now())} days ago"
                                            it.owner?.let {
                                                val flag = convertCountryCodeToEmojiFlag(it.country)
                                                Text(
                                                    text = "by ${it.username} $flag $private - $ago",
                                                    style = TextStyle.Default.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Light
                                                    )
                                                )
                                            }
                                        }
                                        Row {
                                            Spacer(modifier = Modifier.width(24.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Count",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${it.puzzle_count} puzzle(s)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                fun rankToString(rank: Int) = when {
                                                    rank < 30 -> "${30 - rank}k"
                                                    else -> "${rank - 29}d"
                                                }
                                                Text(
                                                    text = "Rank",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text =
                                                        if(it.min_rank == it.max_rank)
                                                            "${rankToString(it.min_rank)}"
                                                        else
                                                            "${rankToString(it.min_rank)} to ${rankToString(it.max_rank)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Row(modifier = Modifier
                                                .align(Alignment.End)
                                            ) {
                                            val solveRate = (it.solved_count*100f) / it.attempt_count
                                            Text(
                                                text = "${it.view_count} views, solved ${it.solved_count} times of ${it.attempt_count} (${"%.2f".format(solveRate)}%)",
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic,
                                                fontWeight = FontWeight.Light,
                                                color = MaterialTheme.colors.onBackground,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                      //if(state.myTurnGames.isNotEmpty()) {
                      //    if(state.myTurnGames.size > 10) {
                      //        item {
                      //            Header("Your turn")
                      //        }
                      //        items (items = state.myTurnGames) {
                      //            SmallGameItem(game = it, state.userId, onAction = onAction)
                      //        }
                      //    } else {
                      //        item {
                      //            MyTurnCarousel(state.myTurnGames, state.userId, onAction)
                      //        }
                      //    }
                      //}

                      //items(items = state.challenges) {
                      //    ChallengeItem(it, state.userId, onAction)
                      //}
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    override val actions: Observable<PuzzleDirectoryAction>
        get() =
            Observable.merge(
                    listOf(
                            internalActions
                    )
            ).startWith(ViewReady)

    override fun render(state: PuzzleDirectoryState) {
        currentState = state
    }

    private fun navigateToPuzzleScreen(collection: PuzzleCollection) {
        Toast.makeText(requireContext(), "${collection.id}", Toast.LENGTH_LONG).show()
      //findNavController()?.navigate(
      //    R.id.puzzleFragment,
      //    bundleOf(
      //        COLLECTION_ID to collection.id,
      //    ),
      //    NavOptions.Builder()
      //        .setLaunchSingleTop(true)
      //        .build()
      //)
    }

    override fun onPause() {
        viewModel.unbind()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
      //analytics.setCurrentScreen(requireActivity(), javaClass.simpleName, null)
        viewModel.bind(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        PersistenceManager.visitedPuzzleDirectory = true
    }

}
