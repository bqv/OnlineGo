package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.zenandroid.onlinego.data.model.local.Player

/**
 * Created by alex on 04/11/2017.
 */
@Entity
data class OGSPlayer (
        @PrimaryKey var id: Long? = null,
        var username: String? = null,
        var ranking: Float? = null,
        var professional: Boolean? = null,
        var accepted_stones: String? = null,
        @Embedded(prefix = "ratings_") var ratings: Ratings? = null,
        var egf: Double? = null,
        var country: String? = null,
        var icon: String? = null,
        var ui_class: String? = null,
        var ladder_rank: Int? = null
) {
    data class Ratings(
            @Embedded(prefix = "overall_") var overall: Rating? = null
    )
    data class Rating(
            var deviation: Double? = null,
            var rating: Double? = null,
            var volatility: Float? = null,
            var games_played: Int? = null
    )

    companion object {
        fun fromPlayer(player: Player) =
            OGSPlayer(
                    id = player.id,
                    username = player.username,
                    ratings = Ratings(Rating(rating = player.rating)),
                    ui_class = player.ui_class
            )
    }
}
