package io.zenandroid.onlinego.data.model.ogs

import io.zenandroid.onlinego.data.model.local.InitialState
import org.threeten.bp.Instant

data class MoveTree (
    val y: Int,
    val x: Int,
    val correct_answer: Boolean?,
    val wrong_answer: Boolean?,
    val text: String?,
    val branches: List<MoveTree>?
)

data class PuzzleData (
    val puzzle_rank: String,
    val name: String,
    val move_tree: MoveTree,
    val initial_player: String,
    val height: Int,
    val width: Int,
    val mode: String,
    val puzzle_collection: String,
    val puzzle_type: String,
    val initial_state: InitialState,
    val puzzle_description: String
)

data class Puzzle (
    val id: Long,
    val order: Float?,
    val owner: PuzzleOwner?,
    val name: String,
    val created: Instant?,
    val modified: String?,
    val puzzle: PuzzleData,
    val private: Boolean?,
    val width: Int?,
    val height: Int?,
    val type: String,
    val has_solution: Boolean,
    val rating: Float?,
    val rating_count: Int?,
    val rank: Int,
    val collection: PuzzleCollection?,
    val view_count: Int?,
    val solved_count: Int?,
    val attempt_count: Int?,

    var playerRating: PuzzleRating?
)
