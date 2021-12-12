package io.zenandroid.onlinego.data.model.local

import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.zenandroid.onlinego.data.model.ogs.PuzzleCollection
import org.threeten.bp.Instant

@Entity
data class VisitedPuzzleCollection(
    var collectionId: Long,
    var timestamp: Instant = Instant.now(),
    var count: Int = 1,
    @PrimaryKey(autoGenerate = true) var _id: Int? = null,
)
