package io.zenandroid.onlinego.ui.screens.puzzle

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

class TsumegoViewModel (
    private val puzzleRepository: PuzzleRepository,
    private val restService: OGSRestService,
    private val store: Store<TsumegoState, TsumegoAction>,
    private val puzzleId: Long
    ): ViewModel()
{
    private val _state = MutableLiveData(TsumegoState())
    val state: LiveData<TsumegoState> = _state
    private val subscriptions = CompositeDisposable()
    var collectionPuzzles by mutableStateOf(emptyList<Puzzle>())
        private set

    private val wiring = store.wire()
    private var viewBinding: Disposable? = null

    private var cursor by mutableStateOf(0)

    override fun onCleared() {
        wiring.dispose()
    }

    init {
        puzzleRepository.getPuzzle(puzzleId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe(this::setPuzzle, this::onError)
            .addToDisposable(subscriptions)
    }

    private fun setPuzzle(puzzle: Puzzle) {
        _state.value = _state.value?.copy(
            puzzle = puzzle
        )

        restService.getPuzzleCollectionContents(puzzle.collection!!.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe(this::setCollection, this::onError)
            .addToDisposable(subscriptions)
    }

    private fun setCollection(puzzles: List<Puzzle>) {
        collectionPuzzles = puzzles
        cursor = puzzles.indexOfFirst { it.id == _state.value?.puzzle?.id }
    }

    val hasNextPuzzle: State<Boolean> = derivedStateOf {
        cursor?.let { it < collectionPuzzles?.size?.let { it - 1 } ?: 0 } == true
    }

    val hasPreviousPuzzle: State<Boolean> = derivedStateOf {
        cursor?.let { it > 0 } == true
    }

    fun nextPuzzle() {
        if(!hasNextPuzzle.value) return

        val index = cursor?.let { it + 1 } ?: 0
        cursor = index
        _state.value = _state.value?.copy(
            puzzle = collectionPuzzles?.get(index)
        )
    }

    fun previousPuzzle() {
        if(!hasPreviousPuzzle.value) return

        val index = cursor?.let { it - 1 } ?: 0
        cursor = index
        _state.value = _state.value?.copy(
            puzzle = collectionPuzzles?.get(index)
        )
    }

    fun bind(view: MviView<TsumegoState, TsumegoAction>) {
        viewBinding = store.bind(view)
    }

    fun unbind() {
        viewBinding?.dispose()
    }

    private fun onError(t: Throwable) {
        android.widget.Toast.makeText(org.koin.core.context.GlobalContext.get().get<android.content.Context>(), "Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
        Log.e(this::class.java.canonicalName, t.message, t)
    }
}
//
//private fun List<Puzzle>.find(puzzle: Puzzle) {
//    var current = this.next()
//    do {
//        if(_collectionPuzzles.value?.get(index)?.id == puzzle.id) break
//        current = this.next()
//    } while(this.hasNext())
//
//    if (current.id != puzzle.id) throw IndexOutOfBoundsException()
//}
