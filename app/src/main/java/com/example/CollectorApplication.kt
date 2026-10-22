package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ItemRepository

class CollectorApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: ItemRepository by lazy { ItemRepository(database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CollectorApplication
            private set
    }
}
