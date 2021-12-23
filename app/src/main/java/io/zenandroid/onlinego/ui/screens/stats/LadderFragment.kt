package io.zenandroid.onlinego.ui.screens.stats

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
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
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
import io.zenandroid.onlinego.ui.theme.OnlineGoTheme
import io.zenandroid.onlinego.data.model.StoneType
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.LadderPlayer
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.utils.PersistenceManager
import io.zenandroid.onlinego.utils.convertCountryCodeToEmojiFlag
import io.zenandroid.onlinego.utils.formatRank
import io.zenandroid.onlinego.utils.nullIfEmpty
import org.commonmark.node.*
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.Instant.now
import org.threeten.bp.temporal.ChronoUnit.*
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.getValue

const val LADDER_ID = "LADDER_ID"

private const val TAG = "LadderFragment"

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalComposeUiApi
class LadderFragment : Fragment() {
    private val viewModel: LadderViewModel by viewModel {
        parametersOf(arguments!!.getLong(LADDER_ID))
    }

    private var currentState: LadderState? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                OnlineGoTheme {
                    val state by viewModel.state.observeAsState()

                    Column (
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        val titleState = remember {
                            val base = "Ladder"
                            derivedStateOf {
                                state?.ladder?.name?.let {
                                    "${base}: ${it}"
                                } ?: base
                            }
                        }
                        TopAppBar(
                            title = {
                                Row {
                                    Text(
                                        text = titleState.value,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (state?.hasLeft != true) {
                                        Button(onClick = { -> viewModel.leaveLadder() },
                                                contentPadding = PaddingValues.Absolute(0.dp),
                                                modifier = Modifier.height(24.dp)) {
                                            Text(text = "Leave")
                                        }
                                    } else {
                                        Button(onClick = { -> viewModel.joinLadder() },
                                                contentPadding = PaddingValues.Absolute(0.dp),
                                                modifier = Modifier.height(24.dp)) {
                                            Text(text = "Rejoin")
                                        }
                                    }
                                }
                            },
                            elevation = 1.dp,
                            navigationIcon = {
                                IconButton(onClick = { findNavController().navigateUp() }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                                }
                            },
                            backgroundColor = MaterialTheme.colors.surface
                        )

                        state?.players?.nullIfEmpty()?.let { players ->
                            val listState = rememberLazyListState()
                            var challengesDialogState by remember {
                                mutableStateOf(null as LadderPlayer?)
                            }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                val ladder = state?.ladder!!
								challengesDialogState?.let { player ->
									item {
										AlertDialog(
											onDismissRequest = {
												challengesDialogState = null
											},
											title = {
												Text(text = "${player.player.username} challenges",
                                                    style = TextStyle.Default.copy(
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
											},
											text = {
                                                Column {
                                                    Text(text = "Incoming",
                                                        style = TextStyle.Default.copy(
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                    player.incoming_challenges.forEach {
                                                        Text(text = it.player.run {
                                                            val flag = convertCountryCodeToEmojiFlag(country)
                                                            val rank = formatRank(ranking?.toDouble())
                                                            username?.let { "#$ladder_rank  |  $it  [$rank]   $flag" } ?: ""
                                                        })
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(text = "Outgoing",
                                                        style = TextStyle.Default.copy(
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                    player.outgoing_challenges.forEach {
                                                        Text(text = it.player.run {
                                                            val flag = convertCountryCodeToEmojiFlag(country)
                                                            val rank = formatRank(ranking?.toDouble())
                                                            username?.let { "#$ladder_rank  |  $it  [$rank]   $flag" } ?: ""
                                                        })
                                                    }
                                                }
											},
											buttons = {
												Row(
													modifier = Modifier.padding(all = 8.dp),
													horizontalArrangement = Arrangement.Center
												) {
													Button(
														modifier = Modifier.fillMaxWidth(),
														onClick = { challengesDialogState = null }
													) {
														Text("Dismiss")
													}
												}
											}
										)
									}
                                }
                                items(items = players + ((players.size + 1) .. ladder.size).map {
                                    LadderPlayer(id = -1, rank = it.toLong())
                                }) {
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(modifier = Modifier
                                                .clickable { -> }
                                                .padding(horizontal = 8.dp, vertical = 8.dp)) {
                                            Text(
                                                text = "#${it.rank}",
                                                modifier = Modifier.width(40.dp)
                                                    .align(Alignment.CenterVertically),
                                                style = TextStyle.Default.copy(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = it.player.run {
                                                    val flag = convertCountryCodeToEmojiFlag(country)
                                                    val rank = formatRank(ranking?.toDouble())
                                                    username?.let { "$it  [$rank]   $flag" } ?: ""
                                                },
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                style = TextStyle.Default.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            if (it.can_challenge.challengeable == true) {
                                                TextButton(onClick = { ->
                                                        viewModel.challengePlayer(it.id)
                                                        findNavController().navigateUp()
                                                        findNavController().navigate(R.id.myGames)
                                                    },
                                                        contentPadding = PaddingValues.Absolute(0.dp),
                                                        modifier = Modifier.height(20.dp)) {
                                                    Text(text = "\u2694\uFE0F")
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            TextButton(onClick = { challengesDialogState = it },
                                                    contentPadding = PaddingValues.Absolute(0.dp),
                                                    modifier = Modifier.height(20.dp)) {
                                                if(it.outgoing_challenges.size > 0) {
                                                    Text(text = "${it.outgoing_challenges.size}")
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            TextButton(onClick = { challengesDialogState = it },
                                                    contentPadding = PaddingValues.Absolute(0.dp),
                                                    modifier = Modifier.height(20.dp)) {
                                                if(it.incoming_challenges.size > 0) {
                                                    Text(text = "${it.incoming_challenges.size}")
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        } ?: run {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "Loading...",
                                    color = MaterialTheme.colors.onBackground,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}
