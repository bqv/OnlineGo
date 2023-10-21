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
data class LadderPlayer (
    val id: Long,
    val rank: Long,
    @Embedded(prefix = "player_") val player: Player,
    @Embedded(prefix = "challengeable_") val can_challenge: Challengeability,

    val ladderId: Long = -1,
    val lastRefresh: Instant = Instant.now(),
) {
    @Ignore var incoming_challenges: List<LadderChallenge> = emptyList()
    @Ignore var outgoing_challenges: List<LadderChallenge> = emptyList()

    @Entity(primaryKeys = ["ladderId","ladderPlayerId"])
    @Immutable
    data class LadderChallenge (
        @Embedded(prefix = "player_") val player: Player,
        val gameId: Long?,

        val ladderId: Long,
        val ladderPlayerId: Long,
        val incoming: Boolean?,
    ) {
        companion object {
            fun fromOGSLadderChallenge(ogsLadderChallenge: OGSLadderChallenge) =
                LadderChallenge(
                    player = ogsLadderChallenge.player.let(Player::fromOGSPlayer),
                    gameId = ogsLadderChallenge.game_id,
                    ladderId = ogsLadderChallenge.ladderId,
                    ladderPlayerId = ogsLadderChallenge.ladderId,
                    incoming = ogsLadderChallenge.incoming,
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
                ladderId = ogsLadderPlayer.ladderId.map(LadderChallenge::fromOGSLadderChallenge),
                lastRefresh = ogsLadderPlayer.lastRefresh.map(LadderChallenge::fromOGSLadderChallenge),
            ).apply {
                incoming_challenges = ogsLadderPlayer.incoming_challenges
                outgoing_challenges = ogsLadderPlayer.outgoing_challenges
            }
    }
}
