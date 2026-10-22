package com.talumi.financas

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=Application::class)
class SalaryWorkerTest {
    @Test fun backgroundWorkerPostsDueSalaryExactlyOnce() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("talumi.db")
        FinanceDb(context).use { db ->
            val account=db.saveAccount(Account(name="Salário"))
            db.addSeries(Series(title="Emprego",amount=300000,kind=EntryKind.INCOME,accountId=account,start=LocalDate.now().withDayOfMonth(1),category="Salário",day=1,autoPost=true))
            db.writableDatabase.execSQL("UPDATE entries SET posted=0")
        }
        val executor=Executors.newSingleThreadExecutor()
        try {
            val worker=TestWorkerBuilder<SalaryWorker>(context,executor).build()
            assertEquals(ListenableWorker.Result.success(),worker.doWork())
            val before=FinanceDb(context).use {it.snapshot()}
            assertEquals(1,before.entries.count {it.posted})
            assertEquals(300000L,Ledger.balance(before.accounts.single(),before.entries))
            assertEquals(ListenableWorker.Result.success(),worker.doWork())
            val after=FinanceDb(context).use {it.snapshot()}
            assertEquals(before,after)
        } finally {executor.shutdownNow();context.deleteDatabase("talumi.db")}
    }
}
