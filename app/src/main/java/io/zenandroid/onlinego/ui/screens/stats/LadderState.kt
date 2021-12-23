package io.zenandroid.onlinego.ui.screens.stats

import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.LadderPlayer

data class LadderState (
    val ladder: Ladder? = null,
    val players: List<LadderPlayer> = emptyList(),
    val hasLeft: Boolean = false
)
