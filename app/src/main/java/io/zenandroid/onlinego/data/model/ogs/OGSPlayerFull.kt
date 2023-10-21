package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.PrimaryKey

data class OGSPlayerFull (
        var user: User,
        var active_games: List<OGSGame> = emptyList(),
        var ladders: List<Ladder> = emptyList(),
        var tournaments: List<Tournament> = emptyList(),
        var titles: List<Any> = emptyList(),
        var trophies: List<Trophy> = emptyList(),
        var groups: List<Group> = emptyList(),
        var is_friend: Boolean? = null,
        var friend_request_sent: Boolean? = null,
        var friend_request_received: Boolean? = null,
        var vs: Versus? = null,
        var block: Block? = null,
        var achievements: List<Any> = emptyList(),
) {
    data class Ladder (
        var id: Long = -1,
        var name: String = "",
        var rank: Int = -1,
    )

    data class Tournament (
        var id: Long = -1,
        var name: String = "",
    )

    data class Trophy (
        var tournament_id: Long = -1,
        var tournament_name: String = "",
        var icon: String = "",
        var title: String = "",
    )

    data class Group (
        var id: Long = -1,
        var name: String = "",
    )

    data class Versus (
        var wins: Int = 0,
        var losses: Int = 0,
        var draw: Int = 0,
        var history: List<Any> = emptyList(),
    )

    data class Block (
        var block_chat: Boolean? = null,
        var block_games: Boolean? = null,
    )

    fun toOGSPlayer() =
        OGSPlayer(
            id = this.user.id,
            username = this.user.username,
            ranking = this.user.ranking,
            professional = this.user.professional,
            ratings = this.user.ratings?.overall?.let { OGSPlayer.Ratings(overall = it) },
            country = this.user.country,
            icon = this.user.icon,
        )
}
