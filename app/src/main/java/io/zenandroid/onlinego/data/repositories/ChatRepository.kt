package io.zenandroid.onlinego.data.repositories

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.data.db.GameDao
import io.zenandroid.onlinego.data.model.local.ChatMetadata
import io.zenandroid.onlinego.data.model.local.Message
import io.zenandroid.onlinego.data.ogs.OGSRestAPI
import io.zenandroid.onlinego.gamelogic.Util.getCurrentUserId
import io.zenandroid.onlinego.utils.addToDisposable

class ChatRepository(
    private val gameDao: GameDao,
    private val restApi: OGSRestAPI,
    ) : SocketConnectedRepository {

    private val knownMessageIds = mutableSetOf<String>()
    private var lastRESTFetchedChatId: String = "00000000-0000-0000-0000-000000000000"
    private val subscriptions = CompositeDisposable()

    init {
        gameDao.getAllMessageIDs()
            .subscribeOn(Schedulers.io())
            .subscribe ( {knownMessageIds.addAll(it) }, {} )
    }

    override fun onSocketConnected() {
        gameDao.monitorChatMetadata()
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .subscribe(this::onMetadata, { onError(it, "monitorHistoricGameMetadata") })
            .addToDisposable(subscriptions)
    }

    private fun onMetadata(metadata: ChatMetadata) {
        lastRESTFetchedChatId = metadata.latestMessageId
    }

    override fun onSocketDisconnected() {
        subscriptions.clear()
    }

    fun addMessage(message: Message) {
        if(!knownMessageIds.contains(message.chatId)) {
            gameDao.insertMessage(message)
            if (message.playerId == getCurrentUserId() && message.gameId != null) {
                gameDao.markGameMessagesAsReadUpTo(message.gameId, message.date)
            }
            knownMessageIds.add(message.chatId)
        }
    }

    fun fetchRecentChatMessages() {
        restApi.getMessages(lastRESTFetchedChatId)
            .map { it.map { Message.fromOGSMessage(it, it.game_id, gameDao.getGame(it.game_id!!).blockingGet().width) } }
            .subscribe(
                gameDao::insertMessagesFromRest,
                { onError(it, "fetchRecentChatMessages") }
            ).addToDisposable(subscriptions)
    }

    fun monitorGameChat(gameId: Long): Flowable<List<Message>> =
        gameDao.getMessagesForGame(gameId)
            .doOnNext { it.forEach { knownMessageIds.add(it.chatId) } }

    fun markMessagesAsRead(messages: List<Message>) {
        Completable
                .create { gameDao.markMessagesAsRead(messages.map { it.chatId }) }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun onError(t: Throwable, request: String) {
        var message = request
        if(t is retrofit2.HttpException) {
            message = "$request: ${t.response()?.errorBody()?.string()}"
            if(t.code() == 429) {
              //FirebaseCrashlytics.getInstance().setCustomKey("HIT_RATE_LIMITER", true)
            }
        }
      //FirebaseCrashlytics.getInstance().recordException(Exception(message, t))
        Log.e("ChatRepository", message, t)
    }

}
