package io.zenandroid.onlinego.ui.screens.puzzle

import io.zenandroid.onlinego.ui.screens.puzzle.PuzzleAction.*
//import io.zenandroid.onlinego.data.model.Puzzle
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.mvi.Reducer

class PuzzleReducer : Reducer<PuzzleState, PuzzleAction> {
    override fun reduce(state: PuzzleState, action: PuzzleAction): PuzzleState {
        /*
        return when (action) {
            is PuzzleLoaded ->
                if(state.lastRequestedNodeId == null || state.lastRequestedNodeId == action.position.node_id) {
                    val history = if(state.position != null && state.position.node_id != action.position.node_id) state.historyStack + state.position else state.historyStack
                    state.copy(
                            position = action.position,
                            description = descriptionOfPuzzle(action.position),
                            boardPuzzle = Puzzle.fromJosekiPuzzle(action.position),
                            historyStack = history,
                            nextPosStack = emptyList(),
                            loading = false,
                            candidateMove = null,
                            error = null,
                            previousButtonEnabled = history.isNotEmpty(),
                            nextButtonEnabled = false,
                            passButtonEnabled = action.position.next_moves?.find { it.placement == "pass" } != null
                    )
                } else {
                    state
                }

            is WaitPuzzle -> state.copy(
                    loading = true,
                    lastRequestedNodeId = action.id,
                    previousButtonEnabled = false
            )

            is ShowCandidateMove -> state.copy(
                    candidateMove = action.placement
            )

            is DataLoadingError -> state.copy(
                    loading = false,
                    error = action.e
            )

            Finish -> state.copy(
                    shouldFinish = true
            )

            UserPressedBack, UserPressedPrevious -> {
                if(state.historyStack.isEmpty()) {
                    state.copy(shouldFinish = true)
                } else {
                    val history = state.historyStack.dropLast(1)
                    val position = state.historyStack.last()
                    val nextPosStack = state.nextPosStack + state.position!!
                    state.copy(
                            position = position,
                            description = descriptionOfPuzzle(position),
                            boardPuzzle = Puzzle.fromJosekiPuzzle(position),
                            historyStack = history,
                            nextPosStack = nextPosStack,
                            loading = false,
                            candidateMove = null,
                            error = null,
                            previousButtonEnabled = history.isNotEmpty(),
                            nextButtonEnabled = true,
                            passButtonEnabled = position.next_moves?.find { it.placement == "pass" } != null
                    )
                }
            }

            UserPressedNext -> {
                val nextPosStack = state.nextPosStack.dropLast(1)
                val position = state.nextPosStack.last()
                val history = state.historyStack + state.position!!
                state.copy(
                        position = position,
                        description = puzzle.name,
                        boardPuzzle = Position.fromPuzzle(puzzle),
                        historyStack = history,
                        nextPosStack = nextPosStack,
                        loading = false,
                        candidateMove = null,
                        error = null,
                        previousButtonEnabled = true,
                        nextButtonEnabled = nextPosStack.isNotEmpty(),
                        passButtonEnabled = position.next_moves?.find { it.placement == "pass" } != null
                )
            }

            is UserTappedCoordinate,
            is LoadPuzzle,
            is UserHotTrackedCoordinate,
            UserPressedPass,
            ViewReady
            -> state
        }
        */ return state
    }
}
