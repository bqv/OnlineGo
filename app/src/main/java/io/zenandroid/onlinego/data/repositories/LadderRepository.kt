package io.zenandroid.onlinego.data.repositories

import android.util.Log
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.data.db.GameDao
import io.zenandroid.onlinego.data.db.SiteDao
import io.zenandroid.onlinego.data.model.local.LadderPlayer
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.gamelogic.Util
import io.zenandroid.onlinego.utils.addToDisposable
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class LadderRepository(
    private val restService: OGSRestService,
    private val dao: SiteDao
) {
    private val TAG = javaClass.simpleName

    private val refreshCooldown = 1.hours.toJavaDuration()

    suspend fun join(id: Long) =
        restService.joinLadder(id = id)

    suspend fun leave(id: Long) =
        restService.leaveLadder(id = id)

    suspend fun challenge(id: Long, playerId: Long) =
        restService.challengeLadderPlayer(id = id, playerId = playerId)

    suspend fun fetchLadder(id: Long): Ladder {
        return restService.getLadder(id)
            .also { persistLadder(it) }
    }

    fun getLadder(id: Long): Flow<Ladder> {
        return dao.getLadder(id)
            .onStart { fetchLadder(id) }
            .catch { onError(it) }
            .onEach { Log.d(TAG, it.toString()) }
            .distinctUntilChanged()
    }

    suspend fun fetchLadderPlayers(ladderId: Long): List<LadderPlayer> {
        return restService.getLadderPlayers(ladderId)
            .map { ladderPlayer ->
                ladderPlayer.copy(
                    ladderId = ladderId,
                    lastRefresh = Instant.now(),
                    incoming_challenges = ladderPlayer.incoming_challenges.map { it.copy(
                        ladderId = ladderId,
                        ladderPlayerId = ladderPlayer.id,
                        incoming = true
                    ) },
                    outgoing_challenges = ladderPlayer.outgoing_challenges.map { it.copy(
                        ladderId = ladderId,
                        ladderPlayerId = ladderPlayer.id,
                        incoming = false
                    ) }
                )
            }
            .toList()
            .also { persistLadderPlayers(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getLadderPlayers(ladderId: Long): Flow<List<LadderPlayer>> {
        withContext(Dispatchers.IO) {
            val lastFetchAgo = dao.getLadderPlayersLastRefreshAgo(ladderId)
            if (lastFetchAgo > refreshCooldown) {
                fetchLadderPlayers(ladderId)
            }
            else Log.d(TAG, "getLadderPlayers debounced: $lastFetchAgo")
        }

        return dao.getLadderPlayers(ladderId)
            .catch { onError(it) }
            .onEach { it.forEach { c -> Log.d(TAG, c.toString()) } }
            .distinctUntilChanged()
            .flatMapConcat {
                it.map { ladderPlayer ->
                    dao.getLadderChallenges(ladderId, ladderPlayer.id)
                        .distinctUntilChanged()
                        .map { list ->
                            ladderPlayer.copy(
                                incoming_challenges = list.filter { it.incoming == true },
                                outgoing_challenges = list.filter { it.incoming == false }
                            )
                        }
                }.let {
                    combine(it) { it.toList() }
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getPlayerLadders(playerId: Long = Util.getCurrentUserId()!!): Flow<List<Ladder>> {
        withContext(Dispatchers.IO) {
            val lastFetchAgo = dao.getPlayerLaddersLastRefreshAgo(playerId)
            if (lastFetchAgo > refreshCooldown) {
                val ladderIdFlow =
                    if (playerId == Util.getCurrentUserId()!!)
                        restService.getParticipatingLadders()
                            .map { it.id }
                    else
                        restService.getPlayerLadders(playerId)
                            .map { it.ladder }

                launch {
                    ladderIdFlow
                        .collect { ladderId ->
                            fetchLadder(ladderId)
                            fetchLadderPlayers(ladderId)
                        }
                }
            }
            else Log.d(TAG, "getPlayerLadders debounced: $lastFetchAgo")
        }

        return dao.getPlayerLadders(playerId)
            .onStart { Log.d(TAG, "getPlayerLadders started") }
            .catch { onError(it) }
            .onEach { Log.d(TAG, "getPlayerLadders: ${it.size}") }
            .onEach { it.forEach { c -> Log.d(TAG, c.toString()) } }
            .distinctUntilChanged()
    }

    private suspend fun persistLadder(ladder: Ladder) {
        dao.insertLadder(ladder)
    }

    private suspend fun persistLadderPlayers(players: List<LadderPlayer>) {
        dao.insertLadderPlayers(players)
        players.forEach {
            dao.replaceLadderChallenges(
                it.ladderId, it.id,
                it.incoming_challenges + it.outgoing_challenges
            )
        }
    }

    private fun onError(error: Throwable) {
        Log.e(TAG, error.message, error)
    }
}
