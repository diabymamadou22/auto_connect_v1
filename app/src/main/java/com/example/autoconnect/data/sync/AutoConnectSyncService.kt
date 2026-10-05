package com.example.autoconnect.data.sync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Android Service to execute background synchronization of mechanic and parts shop data
 * from Cloud Firestore into the local Room SQLite database for offline access.
 */
class AutoConnectSyncService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_SYNC_ALL
        Log.i(TAG, "AutoConnectSyncService started with action: $action, startId: $startId")

        serviceScope.launch {
            try {
                val syncManager = DataSyncManager.getInstance(applicationContext)
                val result = syncManager.syncFromFirestore(forceRefresh = true)
                result.onSuccess { summary ->
                    Log.i(TAG, "Background sync successful: ${summary.totalItems} items stored in Room.")
                }.onFailure { error ->
                    Log.w(TAG, "Background sync encountered an issue: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during background sync: ${e.message}", e)
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "AutoConnectSyncService destroyed")
    }

    companion object {
        private const val TAG = "AutoConnectSyncService"
        const val ACTION_SYNC_ALL = "com.example.autoconnect.action.SYNC_ALL"
        const val ACTION_SYNC_MECHANICS = "com.example.autoconnect.action.SYNC_MECHANICS"
        const val ACTION_SYNC_PARTS = "com.example.autoconnect.action.SYNC_PARTS"

        fun startSync(context: Context) {
            val intent = Intent(context, AutoConnectSyncService::class.java).apply {
                action = ACTION_SYNC_ALL
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not start AutoConnectSyncService: ${e.message}")
            }
        }
    }
}
