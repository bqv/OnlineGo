package io.zenandroid.onlinego.data.db

import android.util.Log
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.zenandroid.onlinego.data.model.local.*
import io.zenandroid.onlinego.data.model.ogs.JosekiPosition
import io.zenandroid.onlinego.data.model.ogs.OGSPlayer
import io.zenandroid.onlinego.data.model.ogs.Phase
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleRating
import io.zenandroid.onlinego.data.model.ogs.PuzzleSolution
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection

/**
 * Created by alex on 04/06/2018.
 */
@Dao
abstract class GameDao {

    @Query("SELECT * FROM game WHERE phase <> 'FINISHED' AND (white_id = :userId OR black_id = :userId)")
    abstract fun monitorActiveGames(userId: Long?) : Flowable<List<Game>>

    @Query("""
        SELECT *
        FROM game 
        LEFT JOIN (
            SELECT 
                gameId, 
                COUNT(*) as messagesCount 
            FROM message 
            WHERE seen <> 1
                AND playerId <> :userId
            GROUP BY gameId
        ) message 
        ON message.gameId == game.id 
        WHERE 
            phase <> 'FINISHED' 
            AND (white_id = :userId OR black_id = :userId)
    """)
    abstract fun monitorActiveGamesWithNewMessagesCount(userId: Long?) : Flowable<List<Game>>

    @Query("""
        SELECT * 
        FROM game 
        LEFT JOIN (
            SELECT 
                gameId, 
                COUNT(*) as messagesCount 
            FROM message 
            WHERE seen <> 1 
                AND playerId <> :userId
            GROUP BY gameId
        ) message 
        ON message.gameId == game.id 
        WHERE 
            phase = 'FINISHED' 
            AND (white_id = :userId OR black_id = :userId)
            AND ended > (SELECT STRFTIME('%s','now','-3 days') * 1000000)
        ORDER BY ended DESC 
        LIMIT 25
        """)
    abstract fun monitorRecentGames(userId: Long?) : Flowable<List<Game>>

    @Query("""
        SELECT * 
        FROM game 
        WHERE 
            phase = 'FINISHED' 
            AND (white_id = :userId OR black_id = :userId)
            AND id NOT IN (
                SELECT id 
                FROM game
                WHERE 
                    phase = 'FINISHED' 
                    AND (white_id = :userId OR black_id = :userId)
                    AND ended > (SELECT STRFTIME('%s','now','-3 days') * 1000000)
                    ORDER BY ended DESC 
                    LIMIT 25
                )
        ORDER BY ended DESC 
        LIMIT 10
        """)
    abstract fun monitorFinishedNotRecentGames(userId: Long?) : Flowable<List<Game>>

    @Query("SELECT * FROM game WHERE phase = 'FINISHED' AND (white_id = :userId OR black_id = :userId) AND ended < :endedBefore ORDER BY ended DESC LIMIT 10")
    abstract fun monitorFinishedGamesEndedBefore(userId: Long?, endedBefore: Long) : Flowable<List<Game>>

    @Query("""
        SELECT id 
        FROM game 
        WHERE 
            id in (:ids) 
            AND phase = 'FINISHED' 
            AND outcome <> '' 
            AND outcome IS NOT NULL 
            AND blackLost IS NOT NULL 
            AND whiteLost IS NOT NULL
            AND ended IS NOT NULL
            """)
    abstract fun getHistoricGamesThatDontNeedUpdating(ids: List<Long>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertAllGamesInternal(games: List<Game>)

    @Update
    abstract fun updateGames(games: List<Game>)

    //
    // All of this complication is needed because sometimes the backend responds with
    // incomplete data for the players and we don't want to overwrite good data with bad
    // e.g. icon missing when getting stuff through the /overview call
    //
    @Transaction
    open fun insertAllGames(games: List<Game>) {
        val existingGames = getGameList(games.map(Game::id))
        val newGames = games.filter { candidate ->
            existingGames.find { it.id == candidate.id } == null
        }
        Log.i("GameDao", "Inserting ${newGames.size} games out of ${games.size}")
        insertAllGamesInternal(newGames)

        val updatedGames = mutableListOf<Game>()
        for(oldGame in existingGames) {
            val updatedGame = games.find { it.id == oldGame.id }
            updatedGame?.let {
                if(it.blackPlayer.country == null) {
                    it.blackPlayer = oldGame.blackPlayer
                }
                if(it.whitePlayer.country == null) {
                    it.whitePlayer = oldGame.whitePlayer
                }
                if(it.moves.isNullOrEmpty()) {
                    it.moves = oldGame.moves
                }
                updatedGames.add(it)
            }
        }

        Log.i("GameDao", "Updating ${existingGames.size} games out of ${games.size}")
        updateGames(updatedGames)
    }

    @Transaction
    open fun insertHistoricGames(games: List<Game>, metadata: HistoricGamesMetadata) {
        insertAllGames(games)
        updateHistoricGameMetadata(metadata)
    }

    @Query("SELECT * FROM game WHERE id in (:ids)")
    abstract fun getGameList(ids: List<Long>): List<Game>

    @Update
    abstract fun update(game: Game)

    @Query("UPDATE game SET moves = :moves WHERE id = :id")
    abstract fun updateMovesInternal(id: Long, moves: MutableList<MutableList<Int>>)

    @Transaction
    open fun addMoveToGame(gameId: Long, moveNumber: Int, move: MutableList<Int>) {
        //
        // Careful, moveNumber is 1-based not 0-based. Pascal FTW!!!
        //
        getGame(gameId).blockingGet().let { game ->
            game.moves?.let {
                while(it.size < moveNumber) {
                    it.add(mutableListOf(-1, -1))
                }
                it[moveNumber - 1] = move
                updateMovesInternal(gameId, it)
            }

        }
    }


    @Query("UPDATE game SET phase = :phase WHERE id = :id")
    abstract fun updatePhase(id: Long, phase: Phase)

    @Query("UPDATE game SET removedStones = :stones WHERE id = :id")
    abstract fun updateRemovedStones(id: Long, stones: String?)

    @Query("""
        UPDATE game 
        SET 
            white_acceptedStones = :white_stones, 
            black_acceptedstones= :black_stones 
        WHERE id = :id
        """)
    abstract fun updateRemovedStonesAccepted(id: Long, white_stones: String?, black_stones: String?)

    @Query("UPDATE game SET undoRequested = :moveNo WHERE id = :id")
    abstract fun updateUndoRequested(id: Long, moveNo: Int)

    @Transaction
    open fun updateUndoAccepted(id: Long, moveNo: Int) {
        getGame(id).blockingGet().let {
            it.undoRequested = null
            if(it.moves?.size == moveNo) {
                it.moves?.removeAt(moveNo - 1)
            }

            update(it)
        }
    }

    @Transaction
    open fun updateClock(
            id: Long,
            playerToMoveId: Long?,
            clock: Clock?) {
        getGame(id).blockingGet().let {
            if(it.playerToMoveId != playerToMoveId) {
                it.undoRequested = null
            }
            it.playerToMoveId = playerToMoveId
            it.clock = clock
            when(clock?.newPausedState) {
                true -> it.pausedSince = clock.newPausedSince
                false -> it.pausedSince = null
            }
            update(it)
        }
    }

    @Transaction
    open fun updateGameData(
            id: Long,
            outcome: String?,
            phase: Phase,
            playerToMoveId: Long?,
            initialState: InitialState?,
            whiteGoesFirst: Boolean?,
            moves: MutableList<MutableList<Int>>,
            removedStones: String?,
            whiteScore: Score?,
            blackScore: Score?,
            clock: Clock?,
            blackLost: Boolean?,
            whiteLost: Boolean?,
            ended: Long?, // MICROSECONDS!!!
            undoRequested: Int?) {
        getGame(id).blockingGet().let {
            it.outcome = outcome
            it.phase = phase
            it.playerToMoveId = playerToMoveId
            it.initialState = initialState
            it.whiteGoesFirst = whiteGoesFirst
            it.moves = moves
            it.removedStones = removedStones
            it.whiteScore = whiteScore
            it.blackScore = blackScore
            it.clock = clock
            it.undoRequested = undoRequested
            it.blackLost = blackLost
            it.whiteLost = whiteLost
            if(ended != null) {
                it.ended = ended
            }
            update(it)
        }
    }

    @Query("SELECT * FROM game WHERE id = :id")
    abstract fun monitorGame(id: Long): Flowable<Game>

    @Query("SELECT * FROM game WHERE id = :id")
    abstract fun getGame(id: Long): Single<Game>

    @Query("SELECT * FROM game WHERE id = :id")
    abstract fun getGameMaybe(id: Long): Maybe<Game>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertMessage(message: Message)

    @Query("SELECT * FROM message WHERE gameId = :gameId ORDER BY date ASC")
    abstract fun getMessagesForGame(gameId: Long): Flowable<List<Message>>

    @Query("SELECT chatId FROM message")
    abstract fun getAllMessageIDs(): Single<List<String>>

    @Query("UPDATE message SET seen = 1 WHERE chatId in (:ids)")
    abstract fun markMessagesAsRead(ids: List<String>)

    @Query("UPDATE message SET seen = 1 WHERE gameId = :gameId AND date <= :date")
    abstract fun markGameMessagesAsReadUpTo(gameId: Long, date: Long)

    @Query("DELETE FROM challenge")
    abstract fun deleteAllChallenges()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllChallenges(list: List<Challenge>)

    @Transaction
    open fun replaceAllChallenges(list: List<Challenge>) {
        deleteAllChallenges()
        insertAllChallenges(list)
    }

    @Query ("SELECT * FROM challenge")
    abstract fun getChallenges(): Flowable<List<Challenge>>

    @Query ("SELECT * FROM gamenotification")
    abstract fun getGameNotifications(): Flowable<List<GameNotificationWithDetails>>

    @Query("""
        SELECT id 
        FROM game 
        WHERE id NOT IN (:ids) 
            AND phase <> 'FINISHED'
            AND (
                white_id = :userId
                OR
                black_id = :userId
            )
    """)
    abstract fun getGamesThatShouldBeFinished(userId: Long?, ids: List<Long>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllGameNotifications(list: List<GameNotification>)

    @Query("DELETE FROM gamenotification")
    abstract fun deleteGameNotifications()

    @Transaction
    open fun replaceGameNotifications(list: List<GameNotification>) {
        deleteGameNotifications()
        insertAllGameNotifications(list)
    }

    @Query("""
        SELECT DISTINCT id, username, icon, rating, country FROM (
            SELECT 
                black_id as id,
                black_username as username,
                black_icon as icon,
                black_rating as rating,
                black_country as country,
                lastMove
            FROM game 
            WHERE black_id <> :userId 
            
            UNION 
            
            SELECT 
                white_id as id,
                white_username as username,
                white_icon as icon,
                white_rating as rating,
                white_country as country,
                lastMove
            FROM game
            WHERE white_id <> :userId
        ) 
        ORDER BY 
            lastMove DESC
        LIMIT 25
    """)
    abstract fun getRecentOpponents(userId: Long?): Single<List<Player>>

    @Transaction
    open fun insertJosekiPositionsWithChildren(fullyLoadedPositions: List<JosekiPosition>, children: List<JosekiPosition>) {
        insertJosekiPositionsReplacingDuplicates(fullyLoadedPositions)
        insertJosekiPositionsIgnoringDuplicates(children)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertJosekiPositionsReplacingDuplicates(positions: List<JosekiPosition>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertJosekiPositionsIgnoringDuplicates(positions: List<JosekiPosition>)

    @Query("SELECT * FROM josekiposition WHERE play = '.root'")
    abstract fun getJosekiRootPosition(): Flowable<JosekiPosition>

    @Query("SELECT * FROM josekiposition WHERE node_id = :posId AND play IS NOT NULL")
    abstract fun getJosekiPostion(posId: Long): Flowable<JosekiPosition>

    @Query("SELECT * FROM josekiposition WHERE parent_id = :parentId")
    abstract fun getChildrenPositions(parentId: Long): List<JosekiPosition>

    @Query("SELECT * FROM historicgamesmetadata WHERE id = 0")
    abstract fun monitorHistoricGameMetadata(): Flowable<HistoricGamesMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateHistoricGameMetadata(metadata: HistoricGamesMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateChatMetadata(metadata: ChatMetadata)

    @Query("SELECT * FROM ChatMetadata WHERE id = 0")
    abstract fun monitorChatMetadata(): Flowable<ChatMetadata>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertMessages(messages: List<Message>)

    @Transaction
    open fun insertMessagesFromRest(messages: List<Message>) {
        if(messages.isNotEmpty()) {
            insertMessages(messages)
            updateChatMetadata(ChatMetadata(0, messages.last().chatId))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPuzzleCollections(collections: List<PuzzleCollection>)

    @Query("SELECT * FROM puzzlecollection")
    abstract fun getAllPuzzleCollections(): Flowable<List<PuzzleCollection>>

    @Query("SELECT count(*) FROM puzzlecollection")
    abstract fun getPuzzleCollectionCount(): Single<Int>

    @Query("SELECT * FROM puzzlecollection WHERE id = :collectionId")
    abstract fun getPuzzleCollection(collectionId: Long): Flowable<PuzzleCollection>

    @Query("SELECT * FROM puzzle WHERE puzzle_puzzle_collection = :collectionId")
    abstract fun getPuzzleCollectionPuzzles(collectionId: Long): Flowable<List<Puzzle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPuzzles(puzzles: List<Puzzle>)

    @Query("SELECT * FROM puzzle WHERE id = :puzzleId")
    abstract fun getPuzzle(puzzleId: Long): Flowable<Puzzle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPuzzleRating(rating: PuzzleRating)

    @Query("SELECT * FROM puzzlerating WHERE puzzleId = :puzzleId")
    abstract fun getPuzzleRating(puzzleId: Long): Flowable<PuzzleRating>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertPuzzleSolutions(solutions: List<PuzzleSolution>)

    @Query("SELECT * FROM puzzlesolution WHERE puzzle = :puzzleId")
    abstract fun getPuzzleSolution(puzzleId: Long): Flowable<List<PuzzleSolution>>

    @Insert
    abstract fun insertPuzzleCollectionVisit(visit: VisitedPuzzleCollection): Completable

    @Query("SELECT collectionId, max(timestamp) timestamp, sum(count) count FROM visitedpuzzlecollection GROUP BY collectionId ORDER BY max(timestamp) DESC")
    abstract fun getRecentPuzzleCollections(): Flowable<List<VisitedPuzzleCollection>>
}
