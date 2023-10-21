package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Ladder (
    @PrimaryKey val id: Long = -1,
    val name: String = "",
    val board_size: Int = 0,
    val size: Int = -1,
    @Embedded(prefix = "group_") val group: LadderGroup? = null,
    val player_rank: Int? = null,
    val player_is_member_of_group: Boolean? = null,
) {
    data class ChallengeRequest (val player_id: Long)

    data class LadderGroup (
        val id: Long,
        val name: String,
        val summary: String = "",
        val require_invitation: Boolean = false,
        val is_public: Boolean = true,
        val hide_details: Boolean? = null,
        val member_count: Int = 0,
        val icon: String
    )
}
