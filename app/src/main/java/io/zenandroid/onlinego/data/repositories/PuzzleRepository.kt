package io.zenandroid.onlinego.data.repositories

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.data.db.GameDao
import io.zenandroid.onlinego.data.model.Position
import io.zenandroid.onlinego.data.model.ogs.Puzzle
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import io.zenandroid.onlinego.data.ogs.OGSRestService

class PuzzleRepository(
        private val restService: OGSRestService,
        private val dao: GameDao
) {

    private val disposable = CompositeDisposable()
    private val customMarkPattern = "<(.):([A-H]|[J-T]\\d{1,2})>".toPattern()
    private val headerWithMissingSpaceRegex = "#(?!\\s|#)".toRegex()

    fun getAllPuzzleCollections(): Flowable<List<PuzzleCollection>> {
        /*
        disposable += restService.getPuzzleCollections()
                .subscribe(this::saveCollectionsToDB, this::onError)

        val dbObservable =
                if(id == null) dao.getJosekiRootPosition()
                else dao.getJosekiPostion(id)

        return dbObservable
                .map(this::extractLabelsFromDescription)
                .doOnNext {
                    it.next_moves = dao.getChildrenPositions(it.node_id ?: 0).map(this::extractLabelsFromDescription)
                }
                .distinctUntilChanged()
        */ return restService.getPuzzleCollections()
            .doOnSuccess { it.forEach{c -> Log.d("PuzzleRepository", c.toString())} }
            .toFlowable()
    }

    fun getPuzzleCollection(id: Long): Flowable<PuzzleCollection> {
        /*
        disposable += restService.getPuzzleCollection(id).withPuzzles()
                .subscribe(this::saveCollectionsToDB, this::onError)

        val dbObservable =
                if(id == null) dao.getJosekiRootPosition()
                else dao.getJosekiPostion(id)

        return dbObservable
                .map(this::extractLabelsFromDescription)
                .doOnNext {
                    it.next_moves = dao.getChildrenPositions(it.node_id ?: 0).map(this::extractLabelsFromDescription)
                }
                .distinctUntilChanged()
        */ return restService.getPuzzleCollection(id).withPuzzles()
            .doOnSuccess { Log.d("PuzzleRepository", it.toString()) }
            .toFlowable()
    }

    fun getPuzzle(id: Long): Flowable<Puzzle> {
        /*
        disposable += restService.getPuzzle(id).withPuzzleRating()
                .subscribe(this::saveCollectionsToDB, this::onError)

        val dbObservable =
                if(id == null) dao.getJosekiRootPosition()
                else dao.getJosekiPostion(id)

        return dbObservable
                .map(this::extractLabelsFromDescription)
                .doOnNext {
                    it.next_moves = dao.getChildrenPositions(it.node_id ?: 0).map(this::extractLabelsFromDescription)
                }
                .distinctUntilChanged()
        */ return restService.getPuzzle(id).withPuzzleRating()
            .doOnSuccess { Log.d("PuzzleRepository", it.toString()) }
            .toFlowable()
    }

    private fun saveCollectionsToDB(list: List<PuzzleCollection>) {
        //dao.insertPuzzleCollections(list)
    }

    private fun onError(error: Throwable) {
        Log.e("PuzzleRepository", error.message, error)
    }

    fun Single<Puzzle>.withPuzzleRating(): Single<Puzzle> =
        flatMap { puzzle ->
            restService.getPuzzleRating(puzzle.id).map {
                puzzle.playerRating = it
                puzzle
            }
        }

    fun Single<PuzzleCollection>.withPuzzles(): Single<PuzzleCollection> =
        flatMap { collection ->
            restService.getPuzzleCollectionContents(collection.id).map {
                collection.puzzles = it
                collection
            }
        }
}
