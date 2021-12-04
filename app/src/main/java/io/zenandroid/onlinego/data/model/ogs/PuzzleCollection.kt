package io.zenandroid.onlinego.data.model.ogs

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.zenandroid.onlinego.data.model.local.InitialState
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import org.threeten.bp.Instant

@Entity
data class PuzzleCollection (
    @PrimaryKey var id: Long = -1,
    @Embedded(prefix = "owner_") var owner: OGSPlayer? = null,
    var name: String = "",
    var created: Instant? = null,
    @ColumnInfo(name = "is_private") var private: Boolean = false,
    var price: String? = null,
    @Embedded(prefix = "starting_puzzle_") var starting_puzzle: StartingPuzzle = StartingPuzzle(),
    var rating: Float = 0f,
    var rating_count: Int = 0,
    var puzzle_count: Int = 0,
    var min_rank: Int = 0,
    var max_rank: Int = 0,
    var view_count: Int = 0,
    var solved_count: Int = 0,
    var attempt_count: Int = 0,
    var color_transform_enabled: Boolean? = null,
    var position_transform_enabled: Boolean? = null,
) {
    data class StartingPuzzle (
        var id: Long = -1,
        @Embedded(prefix = "initial_state_") var initial_state: InitialState = InitialState(),
        var width: Int = 0,
        var height: Int = 0
    )
}
