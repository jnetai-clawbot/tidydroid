package com.jnetaol.tidydroid

import android.app.Application
import com.jnetaol.tidydroid.logger.DebugLogger

class TidyDroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugLogger.init(this)
        DebugLogger.i("TidyDroidApp", "App started", "TD-APP-001", mapOf("version" to "1.0.0"))
    }

    override fun onTerminate() {
        DebugLogger.i("TidyDroidApp", "Terminating", "TD-APP-002")
        DebugLogger.shutdown()
        super.onTerminate()
    }
}
