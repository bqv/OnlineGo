package io.zenandroid.onlinego.data.repositories

import android.util.Log
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.data.db.GameDao
import io.zenandroid.onlinego.data.model.local.LadderPlayer
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.utils.addToDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.Instant

class LadderRepository(
    private val restService: OGSRestService,
    private val dao: GameDao
) {
    private val refreshCooldownSeconds = 60 * 60

    suspend fun join(id: Long) =
        restService.joinLadder(id = id)

    suspend fun leave(id: Long) =
        restService.leaveLadder(id = id)

    suspend fun challenge(id: Long, playerId: Long) =
        restService.challengeLadderPlayer(id = id, playerId = playerId)

    fun getLadder(id: Long): Flow<Ladder> {
        return dao.getLadder(id)
            .onStart {
                val ladder = restService.getLadder(id)
                saveLadderToDB(ladder)
            }
            .catch { onError(it) }
            .onEach { Log.d("LadderRepository", it.toString()) }
            .distinctUntilChanged()
    }

    suspend fun getLadderPlayers(ladderId: Long): Flow<List<LadderPlayer>> {
        withContext(Dispatchers.IO) {
            val lastFetchSecondsAgo = dao.getLadderPlayersLastRefresh(ladderId)
                .let { Instant.now().getEpochSecond() - it }

            if (lastFetchSecondsAgo > refreshCooldownSeconds) {
                val ladderPlayers = restService.getLadderPlayers(ladderId)
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
                saveLadderPlayersToDB(ladderPlayers)
            }
        }

        return dao.getLadderPlayers(ladderId)
            .catch { onError(it) }
            .onEach { it.forEach { c -> Log.d("LadderRepository", c.toString()) } }
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
                    combine(it) { it.toList() as List<LadderPlayer> }
                }
            }
    }

    private suspend fun saveLadderToDB(ladder: Ladder) {
        dao.insertLadder(ladder)
    }

    private suspend fun saveLadderPlayersToDB(players: List<LadderPlayer>) {
        dao.insertLadderPlayers(players)
        players.forEach {
            dao.replaceLadderChallenges(
                it.ladderId, it.id,
                it.incoming_challenges + it.outgoing_challenges
            )
        }
    }

    private fun onError(error: Throwable) {
        Log.e("LadderRepository", error.message, error)
    }
}
