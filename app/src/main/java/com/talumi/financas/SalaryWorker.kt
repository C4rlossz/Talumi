package com.talumi.financas

import android.app.Application
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class TalumiApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
    val financeDb: FinanceDb by lazy { FinanceDb(this) }
    override fun onCreate() {
        super.onCreate()
        val request=PeriodicWorkRequestBuilder<SalaryWorker>(1,TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("talumi-automatic-incomes",ExistingPeriodicWorkPolicy.KEEP,request)
    }
}

class SalaryWorker(context: Context, parameters: WorkerParameters) : Worker(context,parameters) {
    override fun doWork(): Result = try {
        val app=applicationContext
        if(app is TalumiApplication) app.financeDb.materialize()
        else FinanceDb(app).use { it.materialize() }
        Result.success()
    } catch(_: android.database.sqlite.SQLiteException) {
        Result.retry()
    } catch(_: IllegalArgumentException) {
        Result.failure()
    }
}
