package io.zenandroid.onlinego.data.model.ogs

data class Ladder (
    val id: Long,
    val name: String,
    val board_size: Int,
    val size: Int,
    val group: Int? = null,
    val player_rank: Int? = null,
    val player_is_member_of_group: Boolean? = null
) {
    data class ChallengeRequest (var player_id: Long)
}
