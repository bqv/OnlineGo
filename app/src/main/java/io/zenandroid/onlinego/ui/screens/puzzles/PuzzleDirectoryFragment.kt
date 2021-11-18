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
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
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
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.data.repositories.SettingsRepository
import io.zenandroid.onlinego.databinding.FragmentPuzzleDirectoryBinding
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
    private lateinit var binding: FragmentPuzzleDirectoryBinding

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
        binding = FragmentPuzzleDirectoryBinding.inflate(inflater, container, false)
        binding.backArrow.setOnClickListener { findNavController().navigateUp() }

        binding.composable.setContent {
            OnlineGoTheme {
                val listState = rememberLazyListState()
                LazyColumn (
                        state = listState,
                        modifier = Modifier.fillMaxHeight()
                ) {
                    items(items = puzzleRepository.getAllPuzzleCollections().blockingFirst()) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.clickable { -> }) {
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
                                              //.align(Alignment.CenterVertically)
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
                                        Text(
                                            text = "(${it.rating_count})",
                                            color = MaterialTheme.colors.onBackground,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Column(modifier = Modifier
                                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)) {
                                    Column {
                                        Text(
                                            text = it.name,
                                            style = TextStyle.Default.copy(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        val private = if(it.private) "(private)" else ""
                                        it.owner?.let {
                                            val country = convertCountryCodeToEmojiFlag(it.country)
                                            Text(
                                                text = "by ${it.username} ${country} ${private}",
                                                style = TextStyle.Default.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Light
                                                )
                                            )
                                        }
                                    }
                                    Row {
                                        fun rankToString(rank: Int) = when {
                                            rank < 30 -> "${30 - rank}k"
                                            else -> "${rank - 29}d"
                                        }
                                        Column {
                                            Header(text = "∑=${it.puzzle_count} puzzle(s)")
                                            if(it.min_rank == it.max_rank)
                                                Header(text = "σ=${rankToString(it.min_rank)}")
                                            else
                                                Header(text = "σ=${rankToString(it.min_rank)} to ${rankToString(it.max_rank)}")
                                            Header(text = "φ=${DAYS.between(it.created, now())} days ago")
                                        }
                                        Column {
                                            Header(text = "γ=${it.view_count} views")
                                            Header(text = "π=${it.solved_count} times solved")
                                            Header(text = "δ=${it.attempt_count} attempts")
                                        }
                                    }
                                }
                            }
                        }
                    }
                  //item {
                  //    HomeScreenHeader(
                  //            image = state.userImageURL,
                  //            mainText = state.headerMainText,
                  //            subText = state.headerSubText
                  //            )
                  //}
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

        return binding.root
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
