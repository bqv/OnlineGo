package io.zenandroid.onlinego.data.repositories

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.data.db.GameDao
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.LadderPlayer
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.utils.addToDisposable
import org.threeten.bp.Instant

class LadderRepository(
        private val restService: OGSRestService,
        private val dao: GameDao
) {
    private val refreshCooldownSeconds = 60 * 60

    private val disposable = CompositeDisposable()

    fun getLadder(id: Long): Flowable<Ladder> {
        disposable += restService.getLadder(id)
            .subscribe(this::saveLadderToDB, this::onError)

        return dao.getLadder(id)
                .doOnNext { Log.d("LadderRepository", it.toString()) }
                .distinctUntilChanged()
    }

    fun getLadderPlayers(ladderId: Long): Flowable<List<LadderPlayer>> {
        disposable += dao.getLadderPlayersLastRefresh(ladderId)
            .subscribeOn(Schedulers.computation())
            .subscribe({
                if((Instant.now().getEpochSecond() - it) > refreshCooldownSeconds) {
                    disposable += restService.getLadderPlayers(ladderId)
                        .map { it.map { ladderPlayer -> ladderPlayer.copy(
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
                        ) } }
                        .subscribe(this::saveLadderPlayersToDB, this::onError)
                }
            }, this::onError)

        return dao.getLadderPlayers(ladderId)
                .doOnNext { it.forEach{c -> Log.d("LadderRepository", c.toString())} }
                .distinctUntilChanged()
                .flatMap {
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
                        Flowable.zip(it, { it.toList() as List<LadderPlayer> })
                    }
                }
    }

    private fun saveLadderToDB(ladder: Ladder) {
        dao.insertLadder(ladder)
    }

    private fun saveLadderPlayersToDB(players: List<LadderPlayer>) {
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
