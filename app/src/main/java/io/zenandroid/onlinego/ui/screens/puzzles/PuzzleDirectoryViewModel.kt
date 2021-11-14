package io.zenandroid.onlinego.ui.screens.puzzle

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposable
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.mvi.Store

class PuzzleDirectoryViewModel (private val store: Store<PuzzleDirectoryState, PuzzleDirectoryAction>): ViewModel()
{
    private val wiring = store.wire()
    private var viewBinding: Disposable? = null

    override fun onCleared() {
        wiring.dispose()
    }

    fun bind(view: MviView<PuzzleDirectoryState, PuzzleDirectoryAction>) {
        viewBinding = store.bind(view)
    }

    fun unbind() {
        viewBinding?.dispose()
    }
}