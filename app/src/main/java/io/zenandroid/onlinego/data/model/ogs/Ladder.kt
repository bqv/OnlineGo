package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Ladder (
    @PrimaryKey var id: Long = -1,
    var name: String = "",
    var board_size: Int = 0,
    var size: Int = -1,
    @Embedded(prefix = "group_") var group: LadderGroup? = null,
    var player_rank: Int? = null,
    var player_is_member_of_group: Boolean? = null,
) {
    data class ChallengeRequest (var player_id: Long)

    data class LadderGroup (
        var id: Long,
        var name: String,
        var summary: String = "",
        var require_invitation: Boolean = false,
        var is_public: Boolean = true,
        var hide_details: Boolean? = null,
        var member_count: Int = 0,
        var icon: String
    )
}
