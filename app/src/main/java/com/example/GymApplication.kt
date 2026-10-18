package com.example

import android.app.Application
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.repository.GymRepository
import com.example.data.storage.ImageStorageManager

class GymApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val imageStorageManager: ImageStorageManager by lazy {
        ImageStorageManager(this)
    }

    val repository: GymRepository by lazy {
        GymRepository(database, imageStorageManager)
    }

    val backupManager: BackupManager by lazy {
        BackupManager(database)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: GymApplication
            private set
    }
}
