package io.zenandroid.onlinego.data.model.ogs

import androidx.compose.runtime.Immutable
import io.zenandroid.onlinego.data.model.local.Player
import java.time.Instant

@Immutable
data class OGSPlayerProfile (
    val user: Player,
  //val active_games: List<OGSGame> = emptyList(),
  //val ladders: List<Ladder> = emptyList(),
  //val tournaments: List<Tournament> = emptyList(),
  //val titles: List<Any> = emptyList(),
  //val trophies: List<Trophy> = emptyList(),
  //val groups: List<Group> = emptyList(),
  //val is_friend: Boolean? = null,
  //val friend_request_sent: Boolean? = null,
  //val friend_request_received: Boolean? = null,
    val vs: VersusStats,
  //val block: Block? = null,
  //val achievements: List<Any> = emptyList(),
) {
    @Immutable
    data class Ladder (
        val id: Long = -1,
        val name: String = "",
        val rank: Int = -1,
    )

    @Immutable
    data class Tournament (
        val id: Long = -1,
        val name: String = "",
    )

    @Immutable
    data class Trophy (
        val tournament_id: Long = -1,
        val tournament_name: String = "",
        val icon: String = "",
        val title: String = "",
    )

    @Immutable
    data class Group (
        val id: Long = -1,
        val name: String = "",
    )

    @Immutable
    data class Block (
        val block_chat: Boolean? = null,
        val block_games: Boolean? = null,
    )
}

@Immutable
data class VersusStats(
    val draws: Int,
    val losses: Int,
    val wins: Int,
    val history: List<VersusStatsGameHistoryItem>,
) {
    companion object {
        val EMPTY = VersusStats(0, 0, 0, emptyList())
    }
}

@Immutable
data class VersusStatsGameHistoryItem(
    val date: Instant,
    val game: Long,
    val state: String,
)
