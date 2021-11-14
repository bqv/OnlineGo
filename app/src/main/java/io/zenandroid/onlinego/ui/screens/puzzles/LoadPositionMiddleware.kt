package io.zenandroid.onlinego.ui.screens.puzzle

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.ui.screens.puzzle.PuzzleDirectoryAction.*
import io.zenandroid.onlinego.mvi.Middleware
import io.zenandroid.onlinego.data.repositories.JosekiRepository
import org.koin.core.context.GlobalContext.get
import org.koin.java.KoinJavaComponent.inject

class LoadPositionMiddleware(
        private val josekiRepository: JosekiRepository
): Middleware<PuzzleDirectoryState, PuzzleDirectoryAction> {
    override fun bind(
            actions: Observable<PuzzleDirectoryAction>,
            state: Observable<PuzzleDirectoryState>
    ): Observable<PuzzleDirectoryAction> {

        return actions.ofType(LoadPosition::class.java)
                .switchMap {
                    josekiRepository.getJosekiPosition(it.id)
                            .subscribeOn(Schedulers.io())
                            .map<PuzzleDirectoryAction>(::PositionLoaded)
                            .onErrorReturn(::DataLoadingError)
                            .toObservable()
                            .startWith(StartDataLoading(it.id))
                }
    }
}