package io.zenandroid.onlinego.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.zenandroid.onlinego.data.db.GameDao
import io.zenandroid.onlinego.ui.screens.main.MainActivity
import io.zenandroid.onlinego.data.model.local.Game
import io.zenandroid.onlinego.data.model.local.GameNotification
import io.zenandroid.onlinego.data.model.local.GameNotificationWithDetails
import io.zenandroid.onlinego.data.repositories.ActiveGamesRepository
import io.zenandroid.onlinego.data.repositories.ChallengesRepository
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.utils.NotificationUtils
import io.zenandroid.onlinego.utils.NotificationUtils.Companion.notifyGames
import io.zenandroid.onlinego.utils.NotificationUtils.Companion.notifyChallenges
import org.koin.core.context.GlobalContext.get
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

private const val NOT_CHARGING_PERIOD_MINUTES = 30L
private const val CHARGING_PERIOD_MINUTES = 4L

private const val NOT_CHARGING_WORK_NAME = "poll_active_games"
private const val CHARGING_WORK_NAME = "poll_active_games_charging"

class SynchronizeGamesWork(val context: Context, params: WorkerParameters) : RxWorker(context, params) {

    companion object {
        fun schedule() {
            scheduleCharging()
            scheduleNotCharging()
        }

        fun unschedule() {
            WorkManager.getInstance().cancelUniqueWork(NOT_CHARGING_WORK_NAME)
            WorkManager.getInstance().cancelUniqueWork(CHARGING_WORK_NAME)
        }

        private fun scheduleNotCharging() {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val request = OneTimeWorkRequestBuilder<SynchronizeGamesWork>()
                    .setInitialDelay(NOT_CHARGING_PERIOD_MINUTES, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance()
                    .enqueueUniqueWork(NOT_CHARGING_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        private fun scheduleCharging() {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(true)
                    .build()
            val request = OneTimeWorkRequestBuilder<SynchronizeGamesWork>()
                    .setInitialDelay(CHARGING_PERIOD_MINUTES, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance()
                    .enqueueUniqueWork(CHARGING_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }

    private val TAG = SynchronizeGamesWork::class.java.simpleName

    private val task = CheckNotificationsTask(context)

    private val userSessionRepository: UserSessionRepository = get().get()

    override fun createWork(): Single<Result> {
        Log.i(TAG, "Started checking for active games")
        if (!userSessionRepository.isLoggedIn()) {
            Log.v(TAG, "Not logged in, giving up")
            return Single.just(Result.failure())
        }
        if (MainActivity.isInForeground) {
            Log.v(TAG, "App is in foreground, giving up")
            return Single.just(Result.success()).reschedule()
        }
        return task.doWork().reschedule()
    }

    private fun <T : Any> Single<T>.reschedule(): Single<T> {
        return this.doFinally {
            try {
                Log.i(TAG, "Enqueue work")
                schedule()
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }
}
