package io.zenandroid.onlinego.data.model.ogs

import io.zenandroid.onlinego.data.model.local.InitialState
import org.threeten.bp.Instant

data class PenData (
    val color: String?,
    val points: List<Int>?
)

data class MarkData (
    val letter: String?,
    val transient_letter: String?,
    val subscript: String?,
    val color: String?,
  //val score: String?, // or bool
    val triangle: Boolean = false,
    val square: Boolean = false,
    val circle: Boolean = false,
    val cross: Boolean = false,
  //val blue_move: Boolean = false,
    val chat_triangle: Boolean = false,
    val sub_triangle: Boolean = false,
    val remove: Boolean = false,
    val stone_removed: Boolean = false,
    val mark_x: Boolean = false,
    val hint: Boolean = false,
    val black: Boolean?,
    val white: Boolean?,
) {
    override fun toString(): String {
        return letter ?: transient_letter ?: subscript ?: color
        ?: when {
            triangle || chat_triangle || sub_triangle -> "△"
            square -> "□"
            circle -> "○"
            cross -> "⨯"
            else -> ""
        }
    }
}

data class Mark (
    val y: Int,
    val x: Int,
    val marks: MarkData,
)

data class MoveTree (
    val y: Int,
    val x: Int,
    val correct_answer: Boolean?,
    val wrong_answer: Boolean?,
    val text: String?,
    val branches: List<MoveTree>?,
    val marks: List<Mark>?,
    val pen_marks: List<PenData>?
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
