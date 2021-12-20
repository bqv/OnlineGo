package io.zenandroid.onlinego.data.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
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
    private val refreshCooldownSeconds = 60 * 60 * 24

    data class LadderReference (
        var ladder: Ladder,
        var players: MutableList<LadderPlayer> = mutableListOf(),
        var lastRefresh: Instant = Instant.now()
    )

    fun LadderReference.join(): Completable =
        restService.joinLadder(id = ladder.id)

    fun LadderReference.leave(): Completable =
        restService.leaveLadder(id = ladder.id)

    fun LadderReference.challenge(id: Long): Completable =
        restService.challengeLadderPlayer(id = ladder.id, playerId = id)

    private var ladders = mutableMapOf<Long, LadderReference>()

    fun getLadder(id: Long): Flowable<LadderReference> {
        return Flowable.just(ladders[id]) ?: restService.getLadder(id)
            .toFlowable()
            .flatMap { it ->
                val reference = LadderReference(it)
                ladders[it.id] = reference
                Flowable.just(reference).concatWith(
                    restService.getLadderPlayers(id)
                        .doOnNext {
                            reference.players.addAll(it)
                        }
                        .map { reference }
                )
            }
    }
}
