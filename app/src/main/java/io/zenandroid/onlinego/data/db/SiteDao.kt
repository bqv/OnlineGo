package io.zenandroid.onlinego.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.zenandroid.onlinego.data.model.Cell
import io.zenandroid.onlinego.data.model.local.Challenge
import io.zenandroid.onlinego.data.model.local.ChallengeNotification
import io.zenandroid.onlinego.data.model.local.ChatMetadata
import io.zenandroid.onlinego.data.model.local.Clock
import io.zenandroid.onlinego.data.model.local.Game
import io.zenandroid.onlinego.data.model.local.GameNotification
import io.zenandroid.onlinego.data.model.local.GameNotificationWithDetails
import io.zenandroid.onlinego.data.model.local.HistoricGamesMetadata
import io.zenandroid.onlinego.data.model.local.InitialState
import io.zenandroid.onlinego.data.model.local.LadderPlayer
import io.zenandroid.onlinego.data.model.local.LadderPlayer.LadderChallenge
import io.zenandroid.onlinego.data.model.local.Message
import io.zenandroid.onlinego.data.model.local.PauseControl
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.model.ogs.JosekiPosition
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.Phase
import io.zenandroid.onlinego.data.ogs.Pause
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SiteDao {
    @Query("SELECT * FROM ladder WHERE id = :ladderId")
    abstract fun getLadder(ladderId: Long): Flow<Ladder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLadder(ladder: Ladder)

    @Query("SELECT * FROM ladderplayer WHERE ladderId = :ladderId ORDER BY rank ASC")
    abstract fun getLadderPlayers(ladderId: Long): Flow<List<LadderPlayer>>

    @Query("SELECT coalesce(max(lastrefresh), 0) FROM ladderplayer WHERE ladderId = :ladderId")
    abstract suspend fun getLadderPlayersLastRefresh(ladderId: Long): Instant

    suspend fun getLadderPlayersLastRefreshAgo(playerId: Long): Duration =
        Duration.between(getLadderPlayersLastRefresh(playerId), Instant.now())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLadderPlayers(ladderPlayers: List<LadderPlayer>)

    @Query("SELECT ladder.* FROM ladderplayer" +
            " INNER JOIN ladder ON ladder.id = ladderPlayer.ladderId" +
            " WHERE ladderPlayer.player_id = :playerId")
    abstract fun getPlayerLadders(playerId: Long): Flow<List<Ladder>>

    @Query("SELECT coalesce(max(lastrefresh), 0) FROM ladderplayer WHERE player_id = :playerId")
    abstract suspend fun getPlayerLaddersLastRefresh(playerId: Long): Instant

    suspend fun getPlayerLaddersLastRefreshAgo(playerId: Long): Duration =
        Duration.between(getPlayerLaddersLastRefresh(playerId), Instant.now())

    @Query("SELECT * FROM ladderchallenge WHERE ladderId = :ladderId AND ladderPlayerId = :ladderPlayerId")
    abstract fun getLadderChallenges(ladderId: Long, ladderPlayerId: Long): Flow<List<LadderChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLadderChallenges(challenges: List<LadderChallenge>)

    @Query("DELETE FROM ladderchallenge WHERE ladderId = :ladderId AND ladderPlayerId = :ladderPlayerId")
    abstract suspend fun deleteLadderChallenges(ladderId: Long, ladderPlayerId: Long)

    @Transaction
    open suspend fun replaceLadderChallenges(ladderId: Long, ladderPlayerId: Long, challenges: List<LadderChallenge>) {
        deleteLadderChallenges(ladderId, ladderPlayerId)
        insertLadderChallenges(challenges)
    }
}
