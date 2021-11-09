package io.zenandroid.onlinego.ui.screens.localai.middlewares

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.mvi.Middleware
import io.zenandroid.onlinego.ui.screens.localai.AiGameAction
import io.zenandroid.onlinego.ui.screens.localai.AiGameAction.*
import io.zenandroid.onlinego.ui.screens.localai.AiGameState
import org.koin.android.ext.android.get

class AnalyticsMiddleware: Middleware<AiGameState, AiGameAction> {
    override fun bind(actions: Observable<AiGameAction>, state: Observable<AiGameState>): Observable<AiGameAction> {
        return actions.withLatestFrom(state)
                .doOnNext { (action, state) ->
                    when(action) {
                        ViewReady, is RestoredState, ViewPaused, ShowNewGameDialog, DismissNewGameDialog, is UserHotTrackedCoordinate, AIOwnershipResponse,
                        HideOwnership -> Unit
                        is NewGame -> {
                            Log.d("ai.state", "ai_game_new_game")
                            state.position?.let {
                                var moves = 0
                                var cursor = it
                                while(cursor.parentPosition != null) {
                                    moves++
                                    cursor = cursor.parentPosition!!
                                }
                                if(moves > it.boardWidth) {
                                    Log.d("ai.state", "ai_game_abandoned_late")
                                } else {
                                    Log.d("ai.state", "ai_game_abandoned_early")
                                }
                            }
                        }
                        EngineStarted -> Log.d("ai.state", "katago_started")
                        EngineStopped -> Log.d("ai.state", "katago_stopped")
                        GenerateAiMove -> Log.d("ai.state", "katago_generate_move")
                        is AIMove -> Log.d("ai.state", "katago_move")
                        AIHint -> Log.d("ai.state", "katago_hint")

                        is ScoreComputed -> if(action.whiteWon) {
                            Log.d("ai.state", "katago_won")
                        } else {
                            Log.d("ai.state", "katago_lost")
                        }

                        is UserTappedCoordinate -> Log.d("ai.state", "ai_game_user_move")
                        UserPressedPrevious -> Log.d("ai.state", "ai_game_user_undo")
                        UserPressedBack -> Log.d("ai.state", "ai_game_user_back")
                        UserPressedNext -> Log.d("ai.state", "ai_game_user_redo")
                        UserPressedPass -> Log.d("ai.state", "ai_game_user_pass")
                        UserAskedForHint -> Log.d("ai.state", "ai_game_user_asked_hint")
                        is EngineWouldNotStart -> Log.d("ai.state", "katago_would_not_start")
                        AIError -> Log.d("ai.state", "katago_error")
                        UserAskedForOwnership -> Log.d("ai.state", "ai_game_user_asked_territory")
                        is UserTriedSuicidalMove -> Log.d("ai.state", "ai_game_user_tried_suicide")
                        is UserTriedKoMove -> Log.d("ai.state", "ai_game_user_tried_ko")
                        is ToggleAIBlack -> Log.d("ai.state", "ai_game_toggle_ai_black")
                        is ToggleAIWhite -> Log.d("ai.state", "ai_game_toggle_ai_white")
                        is PromptUserForMove -> Log.d("ai.state", "ai_game_prompt_user_for_move")
                        is NewPosition -> Log.d("ai.state", "ai_game_new")
                    }
                }
                .switchMap { Observable.empty<AiGameAction>() }
    }

}
