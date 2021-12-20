package io.zenandroid.onlinego.data.model.ogs

data class LadderPlayer (
    val id: Long,
    val rank: Long,
    val player: OGSPlayer,
    val can_challenge: Challengeability,
    val incoming_challenges: List<LadderChallenge> = emptyList(),
    val outgoing_challenges: List<LadderChallenge> = emptyList(),
) {
    data class Challengeability (
        val challengeable: Boolean,
        val reason: String? = null,
        val reason_code: Int? = null,
        val reason_parameter: Any? = null,
    )

    data class LadderChallenge (
        val player: OGSPlayer,
        val game_id: Long,
    )
}
