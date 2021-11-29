package io.zenandroid.onlinego.data.model.ogs

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import io.zenandroid.onlinego.data.model.local.InitialState
import org.threeten.bp.Instant

data class MoveTree (
    var y: Int = -1,
    var x: Int = -1,
    var correct_answer: Boolean? = null,
    var wrong_answer: Boolean? = null,
    var text: String? = null,
    var branches: List<MoveTree>? = null,
    var marks: List<Mark>? = null,
    var pen_marks: List<PenData>? = null
) {
    data class Mark (
        var y: Int,
        var x: Int,
        var marks: MarkData,
    ) {
        data class MarkData (
            var letter: String?,
            var transient_letter: String?,
            var subscript: String?,
            var color: String?,
          //var score: String?, // or bool
            var triangle: Boolean = false,
            var square: Boolean = false,
            var circle: Boolean = false,
            var cross: Boolean = false,
          //var blue_move: Boolean = false,
            var chat_triangle: Boolean = false,
            var sub_triangle: Boolean = false,
            var remove: Boolean = false,
            var stone_removed: Boolean = false,
            var mark_x: Boolean = false,
            var hint: Boolean = false,
            var black: Boolean?,
            var white: Boolean?,
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
    }

    data class PenData (
        var color: String?,
        var points: List<Int>?
    )
}

@Entity
data class Puzzle (
    @PrimaryKey var id: Long = -1,
    var order: Int? = -1,
    @Embedded(prefix = "owner_") var owner: OGSPlayer? = null,
    var name: String = "",
    var created: Instant? = null,
    var modified: Instant? = null,
    @Embedded(prefix = "puzzle_") var puzzle: PuzzleData = PuzzleData(),
    var private: Boolean? = null,
    var width: Int = 0,
    var height: Int = 0,
    var type: String? = null,
    var has_solution: Boolean? = null,
    var rating: Float = 0f,
    var rating_count: Int = 0,
    var rank: Int = 0,
    @Embedded(prefix = "collection_") var collection: PuzzleCollection? = null,
    var view_count: Int = 0,
    var solved_count: Int = 0,
    var attempt_count: Int = 0,
) {
    data class PuzzleData (
        var puzzle_rank: String = "",
        var name: String = "",
        var move_tree: MoveTree = MoveTree(),
        var initial_player: String = "",
        var height: Int = 0,
        var width: Int = 0,
        var mode: String = "",
        var puzzle_collection: String = "",
        var puzzle_type: String = "",
        @Embedded(prefix = "initial_state_") var initial_state: InitialState = InitialState(),
        var puzzle_description: String = ""
    )
}
