package io.zenandroid.onlinego.data.model.ogs

data class PuzzleSolution (

    var id: Long? = null,
    var puzzle: Long,
    var player: OGSPlayer? = null,
    var player_rank: Int? = null,
    var player_rating: Int? = null,
    var time_elapsed: Long?,
    var flipped_horizontally: Boolean = false,
    var flipped_vertically: Boolean = false,
    var transposed: Boolean = false,
    var colors_swapped: Boolean = false,
    var attempts: Int?,
    var solution: String

) {
    var time_taken: Long? // hacky alias
        get() { return this.time_elapsed }
        set(value) { this.time_elapsed = value }
}
