package com.example.autoconnect

import android.app.Application
import com.example.autoconnect.data.local.AppDatabase
import com.example.autoconnect.data.repository.AutoConnectRepository
import java.io.File

class AutoConnectApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { AutoConnectRepository(database, this) }

    override fun onCreate() {
        super.onCreate()
        // Ensure WebView HTTP cache subdirectories exist to avoid Chromium simple_file_enumerator crashes
        try {
            val jsCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!jsCacheDir.exists()) {
                jsCacheDir.mkdirs()
            }
            val wasmCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!wasmCacheDir.exists()) {
                wasmCacheDir.mkdirs()
            }
        } catch (_: Throwable) {}
    }
}
