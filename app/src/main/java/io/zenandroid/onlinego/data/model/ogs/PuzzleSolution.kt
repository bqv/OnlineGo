package io.zenandroid.onlinego.data.model.ogs

data class PuzzleSolution (

    var time_elapsed: Int,
    var flipped_horizontally: Boolean,
    var flipped_vertically: Boolean,
    var transposed: Boolean,
    var colors_swapped: Boolean,
    var attempts: Int,
    var solution: String

)
