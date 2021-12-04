package io.zenandroid.onlinego.ui.screens.puzzle

import io.zenandroid.onlinego.data.model.local.VisitedPuzzleCollection
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import org.threeten.bp.Instant

data class PuzzleDirectoryState (
    val collections: Map<Long, PuzzleCollection> = emptyMap(),
    val recents: Map<Instant, VisitedPuzzleCollection> = emptyMap(),
)
