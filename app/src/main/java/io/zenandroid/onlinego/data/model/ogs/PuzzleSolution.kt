package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class PuzzleSolution (
    @PrimaryKey var id: Long? = null,
    var puzzle: Long = -1,
    @Ignore var player: OGSPlayer? = null,
    var player_rank: Int? = null,
    var player_rating: Int? = null,
    var time_elapsed: Long? = 0,
    var flipped_horizontally: Boolean = false,
    var flipped_vertically: Boolean = false,
    var transposed: Boolean = false,
    var colors_swapped: Boolean = false,
    var attempts: Int? = 0,
    var solution: String = ""
) {
    var time_taken: Long? // hacky alias
        get() { return this.time_elapsed }
        set(value) { this.time_elapsed = value }
}
