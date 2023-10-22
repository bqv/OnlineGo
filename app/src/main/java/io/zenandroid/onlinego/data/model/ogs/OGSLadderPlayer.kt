package io.zenandroid.onlinego.data.model.ogs

import java.time.Instant

data class OGSLadderPlayer (
    val id: Long = -1,
    val rank: Long = 0,
    val player: OGSPlayer = OGSPlayer(),
    val can_challenge: Challengeability = Challengeability(),
    val incoming_challenges: List<OGSLadderChallenge> = emptyList(),
    val outgoing_challenges: List<OGSLadderChallenge> = emptyList(),
) {
    data class Challengeability (
        val challengeable: Boolean = false,
        val reason: String? = null,
        val reason_code: Int? = null,
        val reason_parameter: Int? = null,
    )

    data class OGSLadderChallenge (
        val player: OGSPlayer,
        val game_id: Long?,
    )
}
