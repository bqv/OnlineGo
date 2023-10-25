package io.zenandroid.onlinego.di

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

fun <T : RoomDatabase> RoomDatabase.Builder<T>.logQueries(TAG: String = "SQL"): RoomDatabase.Builder<T> {
  this
    .setQueryCallback(object : RoomDatabase.QueryCallback {
      private var transactionDepth = 0

      override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
        when (sqlQuery) {
          "BEGIN TRANSACTION" -> {
            transactionDepth++
            return
          }
          "BEGIN DEFERRED TRANSACTION" -> {
            transactionDepth++
            return
          }
          "TRANSACTION SUCCESSFUL" -> {
            return
          }
          "END TRANSACTION" -> {
            transactionDepth--
            return
          }
        }
        val indent = List(transactionDepth) { " " }
          .joinToString("")
        Log.d(TAG, "$indent$sqlQuery")
        if (bindArgs.isNotEmpty())
          Log.v(TAG, " $indent${bindArgs.map {
            when (it ?: return@map "NULL") {
              is String -> "`${it as String}`"
              is Array<*> -> "NULL" // idk but it works
              else -> it.toString()
            }
          }}")
      }
    }, Executors.newSingleThreadExecutor())
    .addCallback(object : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d(TAG, "New DB")
      }

      override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d(TAG, "Open DB")
      }
    })
  return this
}
