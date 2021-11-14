package io.zenandroid.onlinego.ui.screens.localai

import android.util.Log
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.StoneType
import io.zenandroid.onlinego.gamelogic.RulesManager
import io.zenandroid.onlinego.mvi.Reducer
import io.zenandroid.onlinego.ui.screens.localai.AiGameAction.*

class AiGameReducer : Reducer<AiGameState, AiGameAction> {
    override fun reduce(state: AiGameState, action: AiGameAction): AiGameState {
        Log.v("AiGame", "reduce action = $action")
        return when(action) {
            EngineStarted -> state.copy(
                    engineStarted = true,
                    chatText = when {
                        state.position == null && state.newGameDialogShown -> "Ready!"
                        state.position == null && !state.newGameDialogShown -> "Use the 'New Game' button to start a new game"
                        else -> state.chatText
                    }
            )
            EngineStopped -> state.copy(engineStarted = false)
            ViewPaused, UserPressedBack, UserPressedPass  -> {
                state
            }
            is ViewReady -> state.copy(
                    chatText = "Give me a second, I'm getting ready...",
            ).let {
                Log.d("AiGameReducer", "Game Loaded")
                action.loadData?.let { data ->
                    it.copy(
                        position = data.position,
                        boardSize = when(data.position?.boardWidth) {
                            data.position?.boardHeight -> data.position?.boardWidth
                            else -> null //nonsquare
                        }!!,
                        handicap = data.handicap ?: 0,
                        enginePlaysBlack = false,
                        enginePlaysWhite = false,
                        chatText = "Game Loaded!",
                        redoPosStack = emptyList()
                    )
                } ?: it
            }
            is NewPosition -> state.copy(
                    position = action.newPos,
                    nextButtonEnabled = false,
                    redoPosStack = emptyList(),
                    boardIsInteractive = false,
                    chatText = when {
                        action.newPos.isGameOver() && state.whiteWon == true -> "Game ended because of two passes. Final score is black ${state.finalBlackScore?.toInt()} to white ${state.finalWhiteScore?.toInt()}. White wins!"
                        action.newPos.isGameOver() && state.whiteWon == false -> "Game ended because of two passes. Final score is black ${state.finalBlackScore?.toInt()} to white ${state.finalWhiteScore?.toInt()}. Black wins!"
                        action.newPos.isGameOver() && state.whiteWon == null -> "Game ended because of two passes. Hang on, I'm computing the final score."
                        else -> state.chatText
                    },
                    showAiEstimatedTerritory = false,
                    showFinalTerritory = action.newPos.isGameOver() && state.whiteWon != null,
                    hintButtonVisible = !action.newPos.isGameOver(),
                    ownershipButtonVisible = !action.newPos.isGameOver()
            )
            is ScoreComputed -> state.copy(
                    position = action.newPos,
                    nextButtonEnabled = false,
                    passButtonEnabled = false,
                    redoPosStack = emptyList(),
                    boardIsInteractive = false,
                    chatText = if (action.whiteWon) "Game ended because of two passes. Final score is black ${action.blackScore} to white ${action.whiteScore}. White wins!"
                               else "Game ended because of two passes. Final score is black ${action.blackScore} to white ${action.whiteScore}. Black wins!",
                    finalWhiteScore = action.whiteScore.toFloat(),
                    finalBlackScore = action.blackScore.toFloat(),
                    whiteWon = action.whiteWon,
                    previousButtonEnabled = true,
                    showAiEstimatedTerritory = false,
                    showFinalTerritory = true,
                    hintButtonVisible = false,
                    ownershipButtonVisible = false,
                    showHints = false,
                    candidateMove = null
            )
            is AIMove -> state.copy(
                    position = action.newPos,
                    nextButtonEnabled = false,
                    chatText = when {
                        action.newPos.isGameOver() && state.whiteWon == true -> "Game ended because of two passes. Final score is black ${state.finalBlackScore?.toInt()} to white ${state.finalWhiteScore?.toInt()}. White wins!"
                        action.newPos.isGameOver() && state.whiteWon == false -> "Game ended because of two passes. Final score is black ${state.finalBlackScore?.toInt()} to white ${state.finalWhiteScore?.toInt()}. Black wins!"
                        action.newPos.isGameOver() && state.whiteWon == null -> "Game ended because of two passes. Hang on, I'm computing the final score."
                        else -> state.chatText
                    }
            )
            GenerateAiMove -> state.copy(
                    boardIsInteractive = false,
                    passButtonEnabled = false,
                    previousButtonEnabled = false,
                    nextButtonEnabled = false,
                    chatText = "I'm thinking..."
            )
            PromptUserForMove -> state.copy(
                    boardIsInteractive = true,
                    passButtonEnabled = true,
                    previousButtonEnabled = state.position?.parentPosition?.parentPosition != null,
                    chatText = when {
                        state.engineStarted && state.position?.lastMove?.x == -1 -> "Pass! Another pass will conclude the game."
                        state.engineStarted && state.position?.nextToMove == StoneType.WHITE -> "White's turn!"
                        state.engineStarted && state.position?.nextToMove == StoneType.BLACK -> "Black's turn!"
                        else -> state.chatText
                    }
            )
            NextPlayerChanged -> state.copy(
                    boardIsInteractive = !state.boardIsInteractive,
                    passButtonEnabled = !state.passButtonEnabled,
                    previousButtonEnabled = !state.previousButtonEnabled,
                    nextButtonEnabled = !state.nextButtonEnabled
            )
            is UserHotTrackedCoordinate -> state.copy(
                    candidateMove = action.coordinate
            )
            is UserTappedCoordinate -> state.copy(
                    candidateMove = null
            )
            is UserTriedKoMove -> state.copy(
                    candidateMove = null,
                    chatText = "That is an illegal KO move. Repeating a position is not allowed. Try again!"
            )
            is UserTriedSuicidalMove -> state.copy(
                    candidateMove = null,
                    chatText = "That move is illegal because you would kill your own group. Try again!"
            )
            AIError -> state.copy(
                    chatText = "An error occurred communicating with the AI"
            )
            UserPressedPrevious -> {
                val newPosition = if(aiMovedLast(state) && !aiOnlyGame(state)) state.position?.parentPosition?.parentPosition!!
                                  else state.position?.parentPosition!!
                state.copy(
                        position = newPosition,
                        redoPosStack = state.redoPosStack + state.position,
                        previousButtonEnabled = newPosition.parentPosition != null,
                        showHints = false,
                        hintButtonVisible = true,
                        ownershipButtonVisible = true,
                        showFinalTerritory = false,
                        showAiEstimatedTerritory = false,
                        nextButtonEnabled = true,
                        boardIsInteractive = true,
                        passButtonEnabled = true,
                        chatText = "Ok, let's try again. Your turn!",
                        whiteWon = null,
                        finalBlackScore = null,
                        finalWhiteScore = null
                )
            }
            UserPressedNext -> state.copy(
                    position = state.redoPosStack.last(),
                    redoPosStack = state.redoPosStack.dropLast(1),
                    previousButtonEnabled = true,
                    showHints = false,
                    nextButtonEnabled = state.redoPosStack.size > 1
            )
            ShowNewGameDialog -> state.copy(
                    newGameDialogShown = true
            )
            DismissNewGameDialog -> state.copy(
                    newGameDialogShown = false,
                    chatText = if(state.position == null) "Use the 'New Game' button to start a new game" else state.chatText
            )
            is NewGame -> state.copy(
                    boardSize = action.size,
                    handicap = action.handicap,
                    enginePlaysBlack = !action.youPlayBlack,
                    enginePlaysWhite = !action.youPlayWhite,
                    newGameDialogShown = false,
                    showHints = false,
                    whiteWon = null,
                    finalWhiteScore = null,
                    finalBlackScore = null,
                    showFinalTerritory = false,
                    hintButtonVisible = true,
                    ownershipButtonVisible = true,
                    showAiEstimatedTerritory = false,
                    nextButtonEnabled = false,
                    passButtonEnabled = false,
                    chatText = "",
                    previousButtonEnabled = false,
                    boardIsInteractive = false,
                    redoPosStack = emptyList(),
                    candidateMove = null
            )
            AIHint -> state.copy(
                    showHints = true,
                    chatText = "Here are a few moves to consider"
            )
            UserAskedForHint -> state.copy(
                    chatText = "Hmmm..."
            )
            is RestoredState -> action.state.copy( // Careful, this stomps on everything not in the list below!!!
                    engineStarted = state.engineStarted
            )
            AIOwnershipResponse -> state.copy(
                    boardIsInteractive = true,
                    showAiEstimatedTerritory = true,
                    chatText = "Here's what I think the territories look like"
            )
            UserAskedForOwnership -> state.copy(
                    boardIsInteractive = false,
                    chatText = "Ok, calculating current territory..."
            )
            HideOwnership -> state.copy(
                    showAiEstimatedTerritory = false,
                    chatText = "Ok, your turn",
                    boardIsInteractive = true
            )
            ToggleAIBlack -> state.copy(
                    enginePlaysBlack = !state.enginePlaysBlack
            )
            ToggleAIWhite -> state.copy(
                    enginePlaysWhite = !state.enginePlaysWhite
            )
            is EngineWouldNotStart -> state.copy(
                    boardIsInteractive = false,
                    hintButtonVisible = false,
                    ownershipButtonVisible = false,
                    chatText = "Error when starting KataGO: '${action.error.message}'"
            )
        }
    }

    private fun aiMovedLast(state: AiGameState): Boolean =
            (state.position?.lastPlayerToMove == StoneType.BLACK && state.enginePlaysBlack) ||
            (state.position?.lastPlayerToMove == StoneType.WHITE && state.enginePlaysWhite)


    private fun aiOnlyGame(state: AiGameState): Boolean =
            state.enginePlaysBlack || state.enginePlaysWhite
}
