package io.zenandroid.onlinego.ui.screens.puzzle

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.ui.screens.puzzle.PuzzleDirectoryAction.*
import io.zenandroid.onlinego.mvi.Middleware

class AnalyticsMiddleware: Middleware<PuzzleDirectoryState, PuzzleDirectoryAction> {
    override fun bind(actions: Observable<PuzzleDirectoryAction>, state: Observable<PuzzleDirectoryState>): Observable<PuzzleDirectoryAction> {
        return actions.withLatestFrom(state)
                .doOnNext { (action, state) ->
                }
                .switchMap { Observable.empty<PuzzleDirectoryAction>() }
    }

}
