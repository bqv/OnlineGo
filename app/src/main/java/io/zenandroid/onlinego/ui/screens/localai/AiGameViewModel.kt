package io.zenandroid.onlinego.ui.screens.localai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.Disposable
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.mvi.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

class AiGameViewModel(private val store: Store<AiGameState, AiGameAction>): ViewModel() {
    private val wiring = store.wire()
    private var viewBinding: Disposable? = null
    private val sharedFlow = MutableSharedFlow<AiGameState>()
    lateinit var stateFlow: StateFlow<AiGameState>
        private set

    init {
        runBlocking { 
            stateFlow = sharedFlow.stateIn(viewModelScope)
        }
    }

    fun emit(state: AiGameState) {
        runBlocking {
            sharedFlow.emit(state)
        }
    }

    override fun onCleared() {
        wiring.dispose()
    }

    fun bind(view: MviView<AiGameState, AiGameAction>) {
        viewBinding = store.bind(view)
    }

    fun unbind() {
        viewBinding?.dispose()
    }
}
