package io.zenandroid.onlinego.ui.screens.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.data.ogs.OGSRestService
import io.zenandroid.onlinego.data.repositories.UserSessionRepository
import io.zenandroid.onlinego.ui.screens.main.MainActivity
import org.koin.android.ext.android.inject

class BubbleActivity : AppCompatActivity() {

    private val userSessionRepository: UserSessionRepository by inject()
    private val ogsRestService: OGSRestService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

}
