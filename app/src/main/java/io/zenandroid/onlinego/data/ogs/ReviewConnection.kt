package io.zenandroid.onlinego.data.ogs

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.zenandroid.onlinego.data.model.Cell
import io.zenandroid.onlinego.data.model.local.Message
import io.zenandroid.onlinego.data.model.local.Score
import io.zenandroid.onlinego.data.model.local.Time
import io.zenandroid.onlinego.data.model.ogs.Chat
import io.zenandroid.onlinego.data.model.ogs.GameData
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.model.ogs.Phase
import io.zenandroid.onlinego.data.repositories.ChatRepository
import io.zenandroid.onlinego.gamelogic.Util
import io.zenandroid.onlinego.gamelogic.Util.getCurrentUserId
import io.zenandroid.onlinego.utils.addToDisposable
import io.zenandroid.onlinego.utils.recordException
import org.koin.core.context.GlobalContext.get
import java.io.Closeable

private const val TAG = "ReviewConnection"
/**
 * Created by alex on 06/11/2017.
 */
class ReviewConnection(
        val reviewId: Long,
        private val connectionLock: Any,
        var includeChat: Boolean,
        fullStateObservable: Flowable<List<ReviewMessage>>,
        reviewMessageObservable: Flowable<ReviewMessage>,
// https://docs.online-go.com/goban/interfaces/ReviewMessage.html
) : Disposable, Closeable {
    private var closed = false
    private var counter = 0

    private val socketService: OGSWebSocketService = get().get()
    private val chatRepository: ChatRepository = get().get()

    private val gameDataSubject = PublishSubject.create<GameData>()
    private val movesSubject = PublishSubject.create<Move>()
    private val clockSubject = PublishSubject.create<OGSClock>()

    val gameData: Observable<GameData> = gameDataSubject.hide()
    val moves: Observable<Move> = movesSubject.hide()
    val clock: Observable<OGSClock> = clockSubject.hide()
    val undoAccepted: Observable<Int> = undoAcceptedSubject.hide()

    var gameAuth: String? = null

    private val subscriptions = CompositeDisposable()

    init {
        gameDataObservable
                .retryOnError("gamedata")
                .doOnNext{ gameAuth = it.auth }
                .subscribe(gameDataSubject::onNext)
                .addToDisposable(subscriptions)

        movesObservable
                .retryOnError("moves")
                .subscribe(movesSubject::onNext)
                .addToDisposable(subscriptions)
        clockObservable
                .retryOnError("clock")
                .subscribe(clockSubject::onNext)
                .addToDisposable(subscriptions)
    }

    private fun <T> Flowable<T>.retryOnError(requestTag: String): Flowable<T> {
        return this.doOnError {
            FirebaseCrashlytics.getInstance().log("E/$TAG: $requestTag error ${it.message}")
            recordException(it)
        }
                .retry()
    }

    override fun close() {
        decrementCounter()
    }

    override fun isDisposed(): Boolean {
        return closed
    }

    override fun dispose() {
        close()
    }

    fun incrementCounter() {
        synchronized(connectionLock) {
            counter++
        }
    }

    fun decrementCounter() {
        synchronized(connectionLock) {
            counter--
            if (counter == 0) {
                subscriptions.clear()
                socketService.disconnectFromReview(reviewId)
                closed = true
            }
        }
    }

    fun append(action: ReviewMessage) {
        socketService.emit("game/move", action)
    }

    fun sendMessage(message: String, moveNumber: Int, moves: Set<Cell>) {
        val stones = moves
            .joinToString(separator = "") {
                Util.getSGFCoordinates(it)
            }
        socketService.emit("review/chat") {
            "body" - message
            "review_id" - reviewId
            "from" - moveNumber
            "moves" - stones
        }
    }
}
