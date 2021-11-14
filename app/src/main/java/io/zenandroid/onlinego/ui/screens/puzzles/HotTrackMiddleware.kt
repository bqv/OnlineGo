package io.zenandroid.onlinego.ui.screens.puzzle

import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import io.zenandroid.onlinego.mvi.Middleware

class HotTrackMiddleware: Middleware<PuzzleDirectoryState, PuzzleDirectoryAction> {
    override fun bind(actions: Observable<PuzzleDirectoryAction>, state: Observable<PuzzleDirectoryState>): Observable<PuzzleDirectoryAction> {
        return actions.ofType(PuzzleDirectoryAction.UserHotTrackedCoordinate::class.java)
                .withLatestFrom(state)
                .filter { (_, state) -> !state.loading }
                .map { (action, _) -> PuzzleDirectoryAction.ShowCandidateMove(action.coordinate) }
    }
}