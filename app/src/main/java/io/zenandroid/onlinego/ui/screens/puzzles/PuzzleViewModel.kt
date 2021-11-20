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

class PuzzleViewModel (
    private val puzzleRepository: PuzzleRepository,
    private val restService: OGSRestService,
    private val store: Store<PuzzleState, PuzzleAction>,
    private val collectionId: Long
    ): ViewModel()
{
    private val _state = MutableLiveData(PuzzleState())
    val state: LiveData<PuzzleState> = _state
    private val subscriptions = CompositeDisposable()

    private val wiring = store.wire()
    private var viewBinding: Disposable? = null

    override fun onCleared() {
        wiring.dispose()
    }

    init {
        puzzleRepository.getPuzzleCollection(collectionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe(this::setCollection, this::onError)
            .addToDisposable(subscriptions)
    }

    private fun setCollection(collection: PuzzleCollection) {
        _state.value = _state.value?.copy(
            collection = collection
        )
    }

    fun bind(view: MviView<PuzzleState, PuzzleAction>) {
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
