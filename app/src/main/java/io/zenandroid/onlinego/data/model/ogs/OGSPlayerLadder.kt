package io.zenandroid.onlinego.data.model.ogs

data class OGSPlayerLadder(
    val ladder: Long,
    val player: OGSPlayer,
    val rank: Int,
    val id: Long = -1,
    val can_challenge: Boolean = false,
    val active_games: List<LadderGame> = emptyList(),
) {
    data class LadderGame(
        val id: Long,
        val name: String,
        val opponent: String,
        val opponent_id: Long,
        val opponent_rank: Double,
    )
}
