package io.zenandroid.onlinego.ui.screens.puzzle

import android.graphics.Point
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection

data class PuzzleState (
    val collection: PuzzleCollection? = null,
    val puzzles: List<Puzzle>? = null,
)
