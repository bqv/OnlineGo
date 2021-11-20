package io.zenandroid.onlinego.ui.screens.puzzle

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.ui.screens.puzzle.PuzzleAction.*
import io.zenandroid.onlinego.mvi.Middleware

class PuzzleAnalyticsMiddleware: Middleware<PuzzleState, PuzzleAction> {
    override fun bind(actions: Observable<PuzzleAction>, state: Observable<PuzzleState>): Observable<PuzzleAction> {
        return actions.withLatestFrom(state)
                .doOnNext { (action, state) ->
                }
                .switchMap { Observable.empty<PuzzleAction>() }
    }

}
