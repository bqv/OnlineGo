package io.zenandroid.onlinego.data.model.local

import androidx.compose.runtime.Immutable;
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.model.ogs.OGSLadderPlayer
import io.zenandroid.onlinego.data.model.ogs.OGSLadderPlayer.Challengeability
import io.zenandroid.onlinego.data.model.ogs.OGSLadderPlayer.OGSLadderChallenge
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import java.time.Instant

@Entity(primaryKeys = ["id","ladderId"],
        indices = [Index(value = ["rank"], unique = true)])
@Immutable
data class LadderPlayer constructor(
    val id: Long,
    val rank: Long,
    @Embedded(prefix = "player_") val player: Player,
    @Embedded(prefix = "challengeable_") val can_challenge: Challengeability,
    val ladderId: Long = -1,
    val lastRefresh: Instant = Instant.now(),

    @Ignore val incoming_challenges: List<LadderChallenge> = emptyList(),
    @Ignore val outgoing_challenges: List<LadderChallenge> = emptyList(),
) {
    constructor(
        id: Long,
        rank: Long,
        player: Player,
        can_challenge: Challengeability,
        ladderId: Long,
        lastRefresh: Instant,
    ): this(
        id = id,
        rank = rank,
        player = player,
        can_challenge = can_challenge,
        ladderId = ladderId,
        lastRefresh = lastRefresh,
        incoming_challenges = emptyList(),
        outgoing_challenges = emptyList(),
    )

    @Entity(primaryKeys = ["ladderId","ladderPlayerId"])
    @Immutable
    data class LadderChallenge (
        @Embedded(prefix = "player_") val player: Player,
        val gameId: Long?,

        val ladderId: Long = -1,
        val ladderPlayerId: Long = -1,
        val incoming: Boolean? = null,
    ) {
        companion object {
            fun fromOGSLadderChallenge(ogsLadderChallenge: OGSLadderChallenge) =
                LadderChallenge(
                    player = ogsLadderChallenge.player.let(Player::fromOGSPlayer),
                    gameId = ogsLadderChallenge.game_id
                )
        }
    }

    companion object {
        fun fromOGSLadderPlayer(ogsLadderPlayer: OGSLadderPlayer) =
            LadderPlayer(
                id = ogsLadderPlayer.id,
                rank = ogsLadderPlayer.rank,
                player = ogsLadderPlayer.player.let(Player::fromOGSPlayer),
                can_challenge = ogsLadderPlayer.can_challenge,
                incoming_challenges = ogsLadderPlayer.incoming_challenges
                    .map(LadderChallenge::fromOGSLadderChallenge),
                outgoing_challenges = ogsLadderPlayer.outgoing_challenges
                    .map(LadderChallenge::fromOGSLadderChallenge),
            )
    }
}
