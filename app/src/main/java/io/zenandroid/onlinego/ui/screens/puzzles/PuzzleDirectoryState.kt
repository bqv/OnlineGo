package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection

data class PuzzleDirectoryState (
    val collections: List<PuzzleCollection> = emptyList(),
)
