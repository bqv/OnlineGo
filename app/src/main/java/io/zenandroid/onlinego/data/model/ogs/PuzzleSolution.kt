package io.zenandroid.onlinego.data.model.ogs

data class PuzzleSolution (

    var id: Long? = null,
    var puzzle: Long? = null,
    var player: OGSPlayer? = null,
    var player_rank: Int? = null,
    var player_rating: Int? = null,
    var time_elapsed: Int?,
    var flipped_horizontally: Boolean,
    var flipped_vertically: Boolean,
    var transposed: Boolean,
    var colors_swapped: Boolean,
    var attempts: Int,
    var solution: String

) {
    var time_taken: Int? // hacky alias
        get() { return this.time_elapsed }
        set(value) { this.time_elapsed = value }
}
