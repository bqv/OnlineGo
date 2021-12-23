package io.zenandroid.onlinego.ui.screens.stats

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.LadderPlayer
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.data.repositories.LadderRepository
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.mvi.Store
import io.zenandroid.onlinego.utils.addToDisposable
import org.koin.core.context.GlobalContext.get

class LadderViewModel (
    private val ladderRepository: LadderRepository,
    private val restService: OGSRestService,
    private val ladderId: Long
): ViewModel() {
    private val _state = MutableLiveData(LadderState())
    val state: LiveData<LadderState> = _state
    private val subscriptions = CompositeDisposable()

    init {
        ladderRepository.getLadder(ladderId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe(this::setLadder, this::onError)
            .addToDisposable(subscriptions)
        ladderRepository.getLadderPlayers(ladderId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe(this::setLadderPlayers, this::onError)
            .addToDisposable(subscriptions)
    }

    private fun setLadder(response: Ladder) {
        _state.value = _state.value?.copy(
            ladder = response,
        )
    }

    private fun setLadderPlayers(response: List<LadderPlayer>) {
        _state.value = _state.value?.copy(
            players = response,
        )
    }

    fun joinLadder() {
        _state.value?.ladder?.let {
            restService.joinLadder(it.id)
        }
    }

    fun leaveLadder() {
        _state.value?.ladder?.let {
            restService.leaveLadder(it.id)
        }
    }

    fun challengePlayer(id: Long) {
        _state.value?.ladder?.let {
            restService.challengeLadderPlayer(it.id, id)
        }
    }

    private fun onError(t: Throwable) {
        Toast.makeText(get().get<Context>(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
        Log.e(this::class.java.canonicalName, t.message, t)
    }
}
