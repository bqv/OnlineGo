package io.zenandroid.onlinego.ui.screens.puzzle

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.graphics.Point
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.StoneType
import io.zenandroid.onlinego.data.model.ogs.MoveTree
import io.zenandroid.onlinego.data.model.ogs.PlayCategory
import io.zenandroid.onlinego.data.model.ogs.PuzzleRating
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.data.repositories.PuzzleRepository
import io.zenandroid.onlinego.gamelogic.RulesManager
import io.zenandroid.onlinego.gamelogic.Util
import io.zenandroid.onlinego.mvi.MviView
import io.zenandroid.onlinego.mvi.Store
import io.zenandroid.onlinego.utils.addToDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private var moveReplyJob: Job? = null

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
            puzzle = puzzle,
            boardPosition = puzzle.puzzle.let {
                RulesManager.newPosition(it.width, it.height, it.initial_state).also { pos ->
                    pos.customMarks.addAll(it.move_tree.marks?.map {
                        Position.Mark(Point(it.x, it.y), it.marks.let {
                            it.letter ?: it.transient_letter
                        }, PlayCategory.LABEL)
                    } ?: emptyList())
                }.also {
                    when(puzzle.puzzle.initial_player) {
                        "white" -> StoneType.WHITE
                        "black" -> StoneType.BLACK
                        else -> null
                    }?.let { side -> it.nextToMove = side }
                }
            },
            continueButtonVisible = false,
            retryButtonVisible = false,
            nodeStack = ArrayDeque(listOf(puzzle.puzzle.move_tree))
        )

        if(collectionPuzzles.size == 0) {
            restService.getPuzzleCollectionContents(puzzle.collection!!.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
                .subscribe(this::setCollection, this::onError)
                .addToDisposable(subscriptions)
        }
    }

    private fun setCollection(puzzles: List<Puzzle>) {
        collectionPuzzles = puzzles
        cursor = puzzles.indexOfFirst { it.id == _state.value?.puzzle?.id }
    }

    val hasNextPuzzle: State<Boolean> = derivedStateOf {
        cursor.let { it < collectionPuzzles.size.let { it - 1 } } == true
    }

    val hasPreviousPuzzle: State<Boolean> = derivedStateOf {
        cursor.let { it > 0 } == true
    }

    fun nextPuzzle() {
        if(!hasNextPuzzle.value) return

        val index = cursor.let { it + 1 }
        cursor = index
        val puzzle = collectionPuzzles.get(index)
        setPuzzle(puzzle)
        fetchRating()
    }

    fun resetPuzzle() {
        val puzzle = _state.value?.puzzle!!
        setPuzzle(puzzle)
    }

    fun previousPuzzle() {
        if(!hasPreviousPuzzle.value) return

        val index = cursor.let { it - 1 }
        cursor = index
        val puzzle = collectionPuzzles.get(index)
        setPuzzle(puzzle)
        fetchRating()
    }

    fun makeMove(move: Point) {
        _state.value?.nodeStack?.let { stack ->
            val branches = stack.lastOrNull()?.branches ?: emptyList()
            val branch = branches.find {
                it.x == move.x && it.y == move.y
            } ?: branches.find { it.x == -1 || it.y == -1 }
            branch?.let { node ->
                moveReplyJob = viewModelScope.launch {
                    var position = state.value?.boardPosition!!
                    position = RulesManager.makeMove(position, position.nextToMove, move)
                        ?: run {
                            _state.value = state.value?.copy(
                                hoveredCell = null
                            )
                            return@launch
                        }
                    position.nextToMove = position.nextToMove.opponent
                    var nodeStack = _state.value!!.nodeStack
                    nodeStack.addLast(node)
                    node.branches?.randomOrNull()?.let {
                        nodeStack.addLast(it)
                        _state.value = _state.value?.copy(
                            boardPosition = position.also { pos ->
                                pos.customMarks.addAll(node.marks?.map {
                                    Position.Mark(Point(it.x, it.y), it.marks.let {
                                        it.letter ?: it.transient_letter
                                    }, PlayCategory.LABEL)
                                } ?: emptyList())
                            },
                            nodeStack = nodeStack,
                            continueButtonVisible = if(it.correct_answer == true) true
                                else _state.value!!.continueButtonVisible,
                            retryButtonVisible = true,
                            hoveredCell = null,
                        )
                        delay(600)
                        position = RulesManager.makeMove(position, position.nextToMove, Point(it.x, it.y))
                            ?: throw RuntimeException("Invalid move ${it.toString()}")
                        position.nextToMove = position.nextToMove.opponent
                    }
                    _state.value = _state.value?.copy(
                        boardPosition = position.also { pos ->
                            pos.customMarks.addAll(node.marks?.map {
                                Position.Mark(Point(it.x, it.y), it.marks.let {
                                    it.letter ?: it.transient_letter
                                }, PlayCategory.LABEL)
                            } ?: emptyList())
                        },
                        nodeStack = nodeStack,
                        continueButtonVisible = if(node.correct_answer == true) true
                            else _state.value!!.continueButtonVisible,
                        retryButtonVisible = true,
                        hoveredCell = null,
                    )
                }
            } ?: run launch@{
                var position = state.value?.boardPosition!!
                position = RulesManager.makeMove(position, position.nextToMove, move)
                    ?: run {
                        _state.value = state.value?.copy(
                            hoveredCell = null
                        )
                        return@launch
                    }
                position.nextToMove = position.nextToMove.opponent
                var nodeStack = _state.value!!.nodeStack
                nodeStack.addLast(null)
                _state.value = _state.value?.copy(
                    boardPosition = position,
                    nodeStack = nodeStack,
                    retryButtonVisible = true,
                    hoveredCell = null,
                )
            }
            Log.d("MoveTree", state.value!!.nodeStack.last()?.branches?.toString() ?: "")
        }
    }

    fun addBoardHints() {
        _state.value = _state.value?.copy(
            boardInteractive = false
        )
        fun isHappyPath(node: MoveTree): Boolean
            = node.correct_answer == true || node.branches?.any { isHappyPath(it) } == true
        val moves = _state.value?.nodeStack?.last()?.branches?.map {
            if(isHappyPath(it)) {
                if(it.correct_answer == true) {
                    Position.Mark(Point(it.x, it.y), "️", PlayCategory.IDEAL)
                } else {
                    Position.Mark(Point(it.x, it.y), "️", PlayCategory.GOOD)
                }
            } else {
                Position.Mark(Point(it.x, it.y), "", PlayCategory.MISTAKE)
            }
        } ?: emptyList()
        var position = _state.value?.boardPosition!!
        position.customMarks.addAll(moves)
        _state.value = _state.value?.copy(
            boardPosition = position,
            boardInteractive = true
        )
    }

    fun rate(value: Int) {
        restService.ratePuzzle(_state.value?.puzzle?.id!!, value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe({ updateRating(value) }, this::onError)
            .addToDisposable(subscriptions)
    }

    fun fetchRating() {
        restService.getPuzzleRating(_state.value?.puzzle?.id!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // TODO: remove?
            .subscribe({ updateRating(it.rating) }, { updateRating(-1) })
            .addToDisposable(subscriptions)
    }

    fun updateRating(value: Int) {
        _state.value = _state.value?.copy(
            puzzle = _state.value?.puzzle?.copy(
                playerRating = PuzzleRating(
                    rating = value
                )
            )
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
