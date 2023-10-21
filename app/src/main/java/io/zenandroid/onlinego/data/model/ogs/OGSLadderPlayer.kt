package io.zenandroid.onlinego.data.model.ogs

import java.time.Instant

data class OGSLadderPlayer (
    var id: Long = -1,
    var rank: Long = 0,
    var player: OGSPlayer = OGSPlayer(),
    var can_challenge: Challengeability = Challengeability(),
    var incoming_challenges: List<LadderChallenge> = emptyList(),
    var outgoing_challenges: List<LadderChallenge> = emptyList(),

    var ladderId: Long = -1,
    var lastRefresh: Instant = Instant.now(),
) {
    data class Challengeability (
        var challengeable: Boolean = false,
        var reason: String? = null,
        var reason_code: Int? = null,
        var reason_parameter: Int? = null,
    )

    data class OGSLadderChallenge (
        var player: OGSPlayer,
        var game_id: Long?,

        var ladderId: Long = -1,
        var ladderPlayerId: Long = -1,
        var incoming: Boolean? = null,
    )
}
