package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.ogs.JosekiPosition

sealed class PuzzleDirectoryAction {
    object ViewReady: PuzzleDirectoryAction()

    class DataLoadingError(
            val e: Throwable
    ): PuzzleDirectoryAction()

    class PositionLoaded(val position: JosekiPosition): PuzzleDirectoryAction()
    class LoadPosition(val id: Long?): PuzzleDirectoryAction()
    class StartDataLoading(val id: Long?): PuzzleDirectoryAction()
    class ShowCandidateMove(val placement: Point?): PuzzleDirectoryAction()
    object Finish: PuzzleDirectoryAction()

    // User actions
    class UserTappedCoordinate(val coordinate: Point): PuzzleDirectoryAction()
    class UserHotTrackedCoordinate(val coordinate: Point): PuzzleDirectoryAction()
    object UserPressedPrevious: PuzzleDirectoryAction()
    object UserPressedBack: PuzzleDirectoryAction()
    object UserPressedNext: PuzzleDirectoryAction()
    object UserPressedPass: PuzzleDirectoryAction()
}