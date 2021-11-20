package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.data.model.StoneType
import io.zenandroid.onlinego.data.model.local.*

data class TsumegoState (
        val puzzle: Puzzle? = null,
        val lastRequestedNodeId: Long? = null,
        val candidateMove: Point? = null,
        val loading: Boolean = false,
        val description: String? = null,
        val boardPosition: Position? = null,
        val error: Throwable? = null,
        val shouldFinish: Boolean = false,
        val previousButtonEnabled: Boolean = false,
        val nextButtonEnabled: Boolean = false,
        val passButtonEnabled: Boolean = false,
        val node: Node? = null,
        val removedStones: Map<Point, StoneType>? = null,
        val hoveredCell: Point? = null,
        val boardInteractive: Boolean = true,
        val retryButtonVisible: Boolean = true,
        val nextButtonVisible: Boolean = false
)
