package io.zenandroid.onlinego.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.zenandroid.onlinego.data.model.local.*
import io.zenandroid.onlinego.data.model.ogs.JosekiPosition
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.data.model.ogs.PuzzleRating
import io.zenandroid.onlinego.data.model.ogs.PuzzleSolution
import io.zenandroid.onlinego.data.model.ogs.Ladder
import io.zenandroid.onlinego.data.model.ogs.LadderPlayer
import io.zenandroid.onlinego.data.model.ogs.LadderPlayer.LadderChallenge

/**
 * Created by alex on 04/06/2018.
 */
@Database(
        entities = [Game::class, Message::class, Challenge::class, GameNotification::class, JosekiPosition::class, HistoricGamesMetadata::class, ChatMetadata::class, PuzzleCollection::class, Puzzle::class, PuzzleRating::class, PuzzleSolution::class, VisitedPuzzleCollection::class, Ladder::class, LadderPlayer::class, LadderChallenge::class],
        version = 17
)
@TypeConverters(DbTypeConverters::class)
abstract class Database: RoomDatabase() {
    abstract fun gameDao(): GameDao
}
