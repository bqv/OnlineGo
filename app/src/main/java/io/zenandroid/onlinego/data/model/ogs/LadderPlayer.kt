package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import java.time.Instant

@Entity(primaryKeys = ["id","ladderId"],
        indices = [Index(value = ["rank"], unique = true)])
data class LadderPlayer (
    var id: Long = -1,
    var rank: Long = 0,
    @Embedded(prefix = "player_") var player: OGSPlayer = OGSPlayer(),
    @Embedded(prefix = "challengeable_") var can_challenge: Challengeability = Challengeability(),
    @Ignore var incoming_challenges: List<LadderChallenge> = emptyList(),
    @Ignore var outgoing_challenges: List<LadderChallenge> = emptyList(),

    var ladderId: Long = -1,
    var lastRefresh: Instant = Instant.now(),
) {
    data class Challengeability (
        var challengeable: Boolean = false,
        var reason: String? = null,
        var reason_code: Int? = null,
        var reason_parameter: Int? = null,
    )

    @Entity(primaryKeys = ["ladderId","ladderPlayerId"])
    data class LadderChallenge (
        @Embedded(prefix = "player_") var player: OGSPlayer,
        var game_id: Long?,

        var ladderId: Long = -1,
        var ladderPlayerId: Long = -1,
        var incoming: Boolean? = null,
    )
}
