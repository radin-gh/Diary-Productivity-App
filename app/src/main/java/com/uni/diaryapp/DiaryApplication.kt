package com.uni.diaryapp

import android.app.Application
import com.uni.diaryapp.data.AppContainer

class DiaryApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)
    }
}