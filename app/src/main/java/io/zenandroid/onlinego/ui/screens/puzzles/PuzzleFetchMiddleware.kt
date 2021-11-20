package io.zenandroid.onlinego.ui.screens.puzzle

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.ui.screens.puzzle.PuzzleAction.*
import io.zenandroid.onlinego.mvi.Middleware
import io.zenandroid.onlinego.data.repositories.PuzzleRepository
import org.koin.core.context.GlobalContext.get
import org.koin.java.KoinJavaComponent.inject

class PuzzleFetchMiddleware(
        private val puzzleRepository: PuzzleRepository
): Middleware<PuzzleState, PuzzleAction> {
    override fun bind(
            actions: Observable<PuzzleAction>,
            state: Observable<PuzzleState>
    ): Observable<PuzzleAction> {

        return actions.ofType(LoadPuzzle::class.java)
                .switchMap {
                    puzzleRepository.getPuzzle(it.id)
                            .subscribeOn(Schedulers.io())
                            .map<PuzzleAction>(::PuzzleLoaded)
                            .onErrorReturn(::DataLoadingError)
                            .toObservable()
                            .startWith(WaitPuzzle(it.id))
                }
    }
}
