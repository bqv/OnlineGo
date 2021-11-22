package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.ogs.MoveTree
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.data.model.ogs.PuzzleSolution
import io.zenandroid.onlinego.data.model.StoneType
import org.threeten.bp.Instant

data class TsumegoState (
        val puzzle: Puzzle? = null,
        val solutions: List<PuzzleSolution> = emptyList(),
        val startTime: Instant? = null,
        val attemptCount: Int = 0,
        val sgfMoves: String = "",
        val candidateMove: Point? = null,
        val boardPosition: Position? = null,
        val previousButtonEnabled: Boolean = false,
        val nextButtonEnabled: Boolean = false,
        val passButtonEnabled: Boolean = false,
        val nodeStack: ArrayDeque<MoveTree?> = ArrayDeque(),
        val removedStones: Map<Point, StoneType>? = null,
        val hoveredCell: Point? = null,
        val boardInteractive: Boolean = true,
        val retryButtonVisible: Boolean = false,
        val continueButtonVisible: Boolean = false
)
