package io.zenandroid.onlinego.data.model.ogs

import io.zenandroid.onlinego.data.ogs.TimeControl
import java.time.Instant

data class Tournament(
    var id: Long,
    var name: String,
    var description: String,
    var director: OGSPlayer? = null,
    var schedule: Any? = null,
    var title: String? = null,
    var tournament_type: String? = null,
    var handicap: Int? = null,
    var rules: String? = null,
    var time_per_move: Int? = null,
    var time_control_parameters: TimeControl? = null,
    var is_open: Boolean? = null,
    var exclude_provisional: Boolean? = null,
    var group: OGSGroup? = null,
    var player_is_member_of_group: Boolean? = null,
    var auto_start_on_max: Boolean? = null,
    var time_start: Instant? = null,
    var players_start: Instant? = null,
    var first_pairing_method: String? = null,
    var subsequent_pairing_method: String? = null,
    var min_ranking: Int? = null,
    var max_ranking: Int? = null,
    var analysis_enabled: Boolean? = null,
    var exclusivity: String? = null,
    var started: Instant? = null,
    var ended: Instant? = null,
    var start_waiting: Instant? = null,
    var board_size: Int? = null,
    var active_round: Int? = null,
    var settings: Map<String, Any?>? = null,
    var rounds: List<TournamentRound>? = null,
    var icon: String? = null,
    var scheduled_rounds: Int? = null,
    var can_administer: Boolean = false,
    var opengotha_standings: Any? = null,
    var player_count: Int? = null,
) {
    data class TournamentRound(
        val round_number: Int = -1,
        val total_matches: Int = -1,
        val finished_matches: Int = -1,
        val byes: Int = -1,
    )

    data class TournamentInvitation(
        val id: Long? = null,
        val request_id: Long? = null,
        val tournament: Tournament? = null,
        val message: String? = null,
    )
}
