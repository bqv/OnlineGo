package io.zenandroid.onlinego.ui.screens.puzzle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.data.repositories.PuzzleRepository
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.mvi.Store
import io.zenandroid.onlinego.utils.addToDisposable

class PuzzleDirectoryViewModel (
    private val puzzleRepository: PuzzleRepository,
    private val restService: OGSRestService,
    private val store: Store<PuzzleDirectoryState, PuzzleDirectoryAction>
    ): ViewModel()
{
    private val _state = MutableLiveData(PuzzleDirectoryState())
    val state: LiveData<PuzzleDirectoryState> = _state
    private val subscriptions = CompositeDisposable()

    private val wiring = store.wire()
    private var viewBinding: Disposable? = null

    override fun onCleared() {
        wiring.dispose()
    }

    init {
        puzzleRepository.getAllPuzzleCollections()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe(this::setCollections, this::onError)
            .addToDisposable(subscriptions)
    }

    private fun setCollections(collections: List<PuzzleCollection>) {
        _state.value = _state.value?.copy(
            collections = collections.sortedBy { it.rating }
        )
    }

    fun bind(view: MviView<PuzzleDirectoryState, PuzzleDirectoryAction>) {
        viewBinding = store.bind(view)
    }

    fun unbind() {
        viewBinding?.dispose()
    }

    private fun onError(t: Throwable) {
        Log.e(this::class.java.canonicalName, t.message, t)
    }
}
