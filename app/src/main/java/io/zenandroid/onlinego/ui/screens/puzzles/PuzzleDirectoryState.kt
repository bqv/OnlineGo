package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection

data class PuzzleDirectoryState (
        val collections: List<PuzzleCollection> = emptyList(),
        val lastRequestedNodeId: Long? = null,
        val candidateMove: Point? = null,
        val loading: Boolean = false,
        val description: String? = null,
        val boardPosition: Position? = null,
        val error: Throwable? = null,
        val shouldFinish: Boolean = false,
        val previousButtonEnabled: Boolean = false,
        val nextButtonEnabled: Boolean = false,
        val passButtonEnabled: Boolean = false
)
