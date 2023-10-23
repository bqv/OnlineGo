package io.zenandroid.onlinego.data.model.ogs

data class OGSPlayerTournament(
    val tournament: Long,
    val player: OGSPlayer,
    val id: Long = -1,
    val points: String? = null, // "1.000",
    val net_points: String? = null, // "1.000",
    val sos: String? = null, // "9.000",
    val sodos: String? = null, // "2.000",
    val rank: Int = -1,
    val disqualified: Boolean = false,
    val resigned: Boolean = false,
    val eliminated: Boolean = false,
)
