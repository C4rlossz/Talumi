package com.talumi.financas

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.YearMonth
import org.json.JSONObject

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=android.app.Application::class)
class FinanceDbTest {
    private lateinit var db:FinanceDb
    private lateinit var context:Context
    private var account=0L
    @Before fun setup() {context=ApplicationProvider.getApplicationContext();context.deleteDatabase("test.db");db=FinanceDb(context,"test.db");account=db.saveAccount(Account(name="Principal",opening=10000))}
    @After fun close() {db.close();context.deleteDatabase("test.db")}
    private fun entry(amount:Long=2500,kind:EntryKind=EntryKind.EXPENSE,posted:Boolean=true,date:LocalDate=LocalDate.now())=Entry(title="Teste",amount=amount,kind=kind,accountId=account,date=date,posted=posted)
    @Test fun createEditDeleteRecalculatesBalance() {
        val id=db.saveEntry(entry());assertEquals(7500L,Ledger.balance(db.snapshot().accounts.first(),db.snapshot().entries))
        db.saveEntry(db.snapshot().entries.first().copy(amount=5000));assertEquals(5000L,Ledger.balance(db.snapshot().accounts.first(),db.snapshot().entries))
        db.deleteEntry(id);assertEquals(10000L,Ledger.balance(db.snapshot().accounts.first(),db.snapshot().entries))
    }
    @Test fun confirmationIsIdempotent() {val id=db.saveEntry(entry(posted=false));db.confirm(id);db.confirm(id);assertEquals(1,db.snapshot().entries.size);assertEquals(7500L,Ledger.balance(db.snapshot().accounts.first(),db.snapshot().entries))}
    @Test fun futureConfirmationIsRejected() {assertThrows(IllegalArgumentException::class.java){db.saveEntry(entry(date=LocalDate.now().plusDays(1)))};assertTrue(db.snapshot().entries.isEmpty())}
    @Test fun transferIsAtomicAndRequiresDifferentAccounts() {
        assertThrows(IllegalArgumentException::class.java){db.saveEntry(entry(kind=EntryKind.TRANSFER).copy(toAccountId=account))}
        val other=db.saveAccount(Account(name="Cofrinho",kind=AccountKind.JAR))
        db.saveEntry(entry(kind=EntryKind.TRANSFER).copy(toAccountId=other))
        val s=db.snapshot();assertEquals(10000L,s.accounts.sumOf {Ledger.balance(it,s.entries)})
    }
    @Test fun usedAccountCannotBeDeleted() {db.saveEntry(entry());assertThrows(IllegalArgumentException::class.java){db.deleteAccount(account)};assertEquals(1,db.snapshot().accounts.size)}
    @Test fun settlementUpdatesDebtAndAccountTogetherAndDeletionReopensDebt() {
        val id=db.saveDebt(Debt(person="Ana",title="Empréstimo",amount=10000,kind=DebtKind.RECEIVABLE,due=LocalDate.now()))
        db.settle(id,account,2500,LocalDate.now());var s=db.snapshot();assertEquals(7500L,Ledger.outstanding(s.debts.first(),s.entries));assertEquals(12500L,Ledger.balance(s.accounts.first(),s.entries))
        assertThrows(IllegalArgumentException::class.java){db.settle(id,account,8000,LocalDate.now())};assertEquals(1,db.snapshot().entries.size)
        db.deleteEntry(s.entries.first().id);s=db.snapshot();assertEquals(10000L,Ledger.outstanding(s.debts.first(),s.entries));assertEquals(10000L,Ledger.balance(s.accounts.first(),s.entries))
    }
    @Test fun payableSettlementWithdrawsFromAccount() {val id=db.saveDebt(Debt(person="Bia",title="Aluguel",amount=3000,kind=DebtKind.PAYABLE,due=LocalDate.now()));db.settle(id,account,3000,LocalDate.now());val s=db.snapshot();assertEquals(7000L,Ledger.balance(s.accounts.first(),s.entries));assertEquals(0L,Ledger.outstanding(s.debts.first(),s.entries))}
    @Test fun monthlyGenerationDoesNotDuplicateOrResurrectDeletedOccurrence() {
        db.addSeries(Series(title="Salário",amount=500000,kind=EntryKind.INCOME,accountId=account,start=LocalDate.now(),category="Salário"))
        val before=db.snapshot().entries;assertEquals(13,before.size);db.materialize();assertEquals(before.size,db.snapshot().entries.size)
        db.deleteEntry(before.first().id);db.materialize();assertEquals(before.size-1,db.snapshot().entries.size)
        assertTrue(db.snapshot().entries.none {it.posted})
    }
    @Test fun stopSeriesKeepsHistoryAndRemovesOnlyFuturePending() {
        db.addSeries(Series(title="Salário",amount=1000,kind=EntryKind.INCOME,accountId=account,start=LocalDate.now(),category="Salário"))
        db.confirm(db.snapshot().entries.first().id);db.stopSeries(db.snapshot().series.first().id);db.materialize();val s=db.snapshot();assertEquals(1,s.entries.size);assertTrue(s.entries.first().posted);assertFalse(s.series.first().active)
    }
    @Test fun exportRestoreRoundTripPreservesLinksAndAmounts() {
        val other=db.saveAccount(Account(name="Reserva",opening=25000));db.saveEntry(entry(kind=EntryKind.TRANSFER).copy(toAccountId=other))
        val debt=db.saveDebt(Debt(person="Caio",title="Parcela",amount=7000,kind=DebtKind.PAYABLE,due=LocalDate.now()));db.settle(debt,account,1000,LocalDate.now())
        val before=db.snapshot();val backup=db.exportJson();db.saveEntry(entry(999));db.importJson(backup);assertEquals(before,db.snapshot())
    }
    @Test fun invalidBackupRollsBackAllReplacements() {
        db.saveEntry(entry());val before=db.snapshot();val broken=JSONObject(db.exportJson());broken.getJSONArray("entries").getJSONObject(0).put("account_id",99999)
        assertThrows(Exception::class.java){db.importJson(broken.toString())};assertEquals(before,db.snapshot())
    }
    @Test fun overpaidBackupIsRejectedAtomically() {
        val id=db.saveDebt(Debt(person="Ana",title="Parcela",amount=3000,kind=DebtKind.RECEIVABLE,due=LocalDate.now()));db.settle(id,account,1000,LocalDate.now());val before=db.snapshot()
        val bad=JSONObject(db.exportJson());bad.getJSONArray("entries").getJSONObject(0).put("amount",4000)
        assertThrows(Exception::class.java){db.importJson(bad.toString())};assertEquals(before,db.snapshot())
    }
    @Test fun recordsSurviveReopeningDatabase() {db.saveEntry(entry());val before=db.snapshot();db.close();db=FinanceDb(context,"test.db");assertEquals(before,db.snapshot())}
    @Test fun salaryPostsOnDueDayAndDoesNotDuplicateAfterReopen() {
        var clock=LocalDate.of(2024,1,1)
        db.close();db=FinanceDb(context,"test.db") {clock}
        db.addSeries(Series(title="Salário",amount=250000,kind=EntryKind.INCOME,accountId=account,start=clock,category="Salário",day=5,autoPost=true))
        assertTrue(db.snapshot().entries.none {it.posted})
        clock=LocalDate.of(2024,1,5);db.materialize();val paid=db.snapshot().entries.filter {it.posted};assertEquals(1,paid.size);assertEquals(clock,paid.single().date)
        db.close();db=FinanceDb(context,"test.db") {clock};db.materialize();assertEquals(1,db.snapshot().entries.count {it.posted})
        assertEquals(260000L,Ledger.balance(db.snapshot().accounts.single(),db.snapshot().entries,clock))
        clock=LocalDate.of(2024,3,10);db.materialize();assertEquals(3,db.snapshot().entries.count {it.posted})
    }
    @Test fun editingAutomaticOccurrenceToManualIsRespected() {
        var clock=LocalDate.of(2024,1,1);db.close();db=FinanceDb(context,"test.db") {clock}
        db.addSeries(Series(title="Salário",amount=1000,kind=EntryKind.INCOME,accountId=account,start=clock,category="Salário",day=5,autoPost=true))
        val first=db.snapshot().entries.first();db.saveEntry(first.copy(title="Salário atrasado",autoPost=false))
        clock=LocalDate.of(2024,1,10);db.materialize();assertFalse(db.snapshot().entries.first().posted)
        clock=LocalDate.of(2024,2,10);db.materialize();assertEquals(1,db.snapshot().entries.count {it.posted})
    }
    @Test fun deletingAutomaticSalaryDoesNotResurrectIt() {
        var clock=LocalDate.of(2024,1,1);db.close();db=FinanceDb(context,"test.db") {clock}
        db.addSeries(Series(title="Salário",amount=1000,kind=EntryKind.INCOME,accountId=account,start=clock,category="Salário",day=5,autoPost=true))
        val id=db.snapshot().entries.first().id;db.deleteEntry(id);clock=LocalDate.of(2024,1,10);db.materialize();assertTrue(db.snapshot().entries.none {it.posted || it.id==id})
    }
    @Test fun convertingOldRecurrenceDoesNotDuplicateOrConfirmOldArrears() {
        val clock=LocalDate.of(2024,1,1);db.close();db=FinanceDb(context,"test.db") {clock}
        db.addSeries(Series(title="Salário",amount=1000,kind=EntryKind.INCOME,accountId=account,start=clock.minusMonths(1),category="Salário"))
        val before=db.snapshot();db.enableAutomaticIncome(before.series.single().id);val after=db.snapshot()
        assertEquals(before.entries.size,after.entries.size);assertEquals(before.entries.map {it.id},after.entries.map {it.id})
        assertFalse(after.entries.first().posted);assertTrue(after.entries.first {it.date==clock}.posted)
    }
    @Test fun versionOneBackupStillRestoresWithoutTurningOnAutomation() {
        db.addSeries(Series(title="Entrada mensal",amount=1000,kind=EntryKind.INCOME,accountId=account,start=LocalDate.now(),category="Outros"))
        val before=db.snapshot();val old=JSONObject(db.exportJson()).put("version",1)
        val series=old.getJSONArray("series");for(i in 0 until series.length()) for(key in listOf("pay_rule","pay_day","auto_post","include_saturday","holidays")) series.getJSONObject(i).remove(key)
        val entries=old.getJSONArray("entries");for(i in 0 until entries.length()) entries.getJSONObject(i).remove("auto_post")
        db.importJson(old.toString());assertEquals(before,db.snapshot())
    }
    @Test fun businessDaySettingsSurviveBackupAndRestore() {
        val first=LocalDate.now().plusMonths(1).withDayOfMonth(1)
        db.addSeries(Series(title="Emprego",amount=1000,kind=EntryKind.INCOME,accountId=account,start=first,category="Salário",rule=PayRule.FIFTH_BUSINESS_DAY,day=5,autoPost=true,includeSaturday=true,holidays=setOf(first)))
        val before=db.snapshot();val backup=db.exportJson();db.importJson(backup);assertEquals(before,db.snapshot())
    }
    @Test fun upgradingVersionOnePreservesAllRecordsAndManualRules() {
        db.close();context.deleteDatabase("test.db")
        context.openOrCreateDatabase("test.db",0,null).use {legacy ->
            legacy.execSQL("CREATE TABLE accounts (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,institution TEXT NOT NULL,kind TEXT NOT NULL,opening INTEGER NOT NULL)")
            legacy.execSQL("CREATE TABLE series (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,amount INTEGER NOT NULL,kind TEXT NOT NULL,account_id INTEGER NOT NULL REFERENCES accounts(id),start TEXT NOT NULL,category TEXT NOT NULL,active INTEGER NOT NULL,generated_until TEXT NOT NULL)")
            legacy.execSQL("CREATE TABLE debts (id INTEGER PRIMARY KEY AUTOINCREMENT,person TEXT NOT NULL,title TEXT NOT NULL,amount INTEGER NOT NULL,kind TEXT NOT NULL,due TEXT NOT NULL,note TEXT NOT NULL)")
            legacy.execSQL("CREATE TABLE entries (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,amount INTEGER NOT NULL,kind TEXT NOT NULL,account_id INTEGER NOT NULL REFERENCES accounts(id),to_account_id INTEGER REFERENCES accounts(id),date TEXT NOT NULL,posted INTEGER NOT NULL,category TEXT NOT NULL,note TEXT NOT NULL,series_id INTEGER REFERENCES series(id),debt_id INTEGER REFERENCES debts(id))")
            legacy.execSQL("INSERT INTO accounts VALUES (7,'Meu banco','Banco','BANK',12345)")
            legacy.execSQL("INSERT INTO series VALUES (9,'Salário',1000,'INCOME',7,'2026-01-05','Salário',1,'2026-12')")
            legacy.execSQL("INSERT INTO debts VALUES (3,'Ana','Parcela',500,'PAYABLE','2026-10-25','')")
            legacy.execSQL("INSERT INTO entries VALUES (11,'Salário',1000,'INCOME',7,NULL,'2026-01-05',1,'Salário','',9,NULL)")
            legacy.version=1
        }
        db=FinanceDb(context,"test.db");val state=db.snapshot()
        assertEquals(3,db.readableDatabase.version);assertEquals(12345L,state.accounts.single().opening);assertEquals(11L,state.entries.single().id);assertTrue(state.entries.single().posted);assertEquals(3L,state.debts.single().id);assertEquals(5,state.series.single().day);assertFalse(state.series.single().autoPost)
    }
}
