package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection

sealed class PuzzleAction {
    object ViewReady: PuzzleAction()

    class DataLoadingError(
        val e: Throwable
    ): PuzzleAction()

    class PuzzleLoaded(val puzzle: Puzzle): PuzzleAction()
    class LoadPuzzle(val id: Long): PuzzleAction()
    class WaitPuzzle(val id: Long): PuzzleAction()
    class ShowCandidateMove(val placement: Point?): PuzzleAction()
    object Finish: PuzzleAction()

    // User actions
    class UserTappedCoordinate(val coordinate: Point): PuzzleAction()
    class UserHotTrackedCoordinate(val coordinate: Point): PuzzleAction()
    object UserPressedPrevious: PuzzleAction()
    object UserPressedBack: PuzzleAction()
    object UserPressedNext: PuzzleAction()
    object UserPressedPass: PuzzleAction()
}
