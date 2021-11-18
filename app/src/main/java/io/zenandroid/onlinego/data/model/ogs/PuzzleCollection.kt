package io.zenandroid.onlinego.data.model.ogs

import io.zenandroid.onlinego.data.model.local.InitialState
import org.threeten.bp.Instant

data class Rating (
    val rating: Float?,
    val deviation: Float?,
    val volatility: Float?
)

data class PuzzleRatings (
    val version: Int,
    val overall: Rating
)

data class PuzzleOwner (
    val id: Long,
    val username: String,
    val country: String?,
    val icon: String,
    val ratings: PuzzleRatings,
    val ranking: Float?,
    val professional: Boolean?,
    val ui_class: String?
)

data class StartingPuzzle (
    val id: Long,
    val initial_state: InitialState,
    val width: Int,
    val height: Int
)

data class PuzzleCollection (
    val id: Long,
    val owner: PuzzleOwner,
    val name: String,
    val created: Instant?,
    val private: Boolean,
    val price: String?,
    val starting_puzzle: StartingPuzzle,
    val rating: Float,
    val rating_count: Int,
    val puzzle_count: Int,
    val min_rank: Int,
    val max_rank: Int,
    val view_count: Int,
    val solved_count: Int,
    val attempt_count: Int,
    val color_transform_enabled: Boolean,
    val position_transform_enabled: Boolean,

    var puzzles: List<Puzzle>?
)
