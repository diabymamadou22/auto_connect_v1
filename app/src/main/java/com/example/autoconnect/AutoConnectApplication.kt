package com.example.autoconnect

import android.app.Application
import com.example.autoconnect.data.local.AppDatabase
import com.example.autoconnect.data.repository.AutoConnectRepository

class AutoConnectApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { AutoConnectRepository(database, this) }
}
