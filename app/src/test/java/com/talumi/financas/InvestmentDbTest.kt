package com.talumi.financas

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=android.app.Application::class)
class InvestmentDbTest {
    private lateinit var db:FinanceDb
    private lateinit var context:Context
    private var cash=0L
    private val today=LocalDate.of(2026,10,22)
    @Before fun setup() {context=ApplicationProvider.getApplicationContext();context.deleteDatabase("invest-test.db");db=FinanceDb(context,"invest-test.db") {today};cash=db.saveAccount(Account(name="Conta",opening=100000))}
    @After fun close() {db.close();context.deleteDatabase("invest-test.db")}
    private fun create(value:Long=50000,locked:Boolean=false)=db.saveInvestment(Account(name="CDB",institution="Banco teste",kind=AccountKind.INVESTMENT,opening=value),Investment(0,product=InvestmentProduct.CDB,mode=YieldMode.CDI_PERCENT,rate="110",valuationDate=today,liquidity=if(locked) Liquidity.MATURITY else Liquidity.DAILY,maturity=if(locked) today.plusDays(30) else null))
    @Test fun depositWithdrawalAndYieldRemainSeparateFromCash() {
        val id=create(0);db.investmentMovement(id,InvestmentAction.DEPOSIT,20000,cash,today,"")
        var s=db.snapshot();assertEquals(80000L,Ledger.available(s));assertEquals(20000L,Ledger.balance(s.accounts.first {it.id==id},s.entries))
        db.investmentMovement(id,InvestmentAction.GAIN,1000,null,today,"")
        s=db.snapshot();assertEquals(80000L,Ledger.available(s));assertEquals(1000L,InvestmentMath.realized(s,YearMonth.from(today)))
        db.investmentMovement(id,InvestmentAction.WITHDRAW,5000,cash,today,"")
        s=db.snapshot();assertEquals(85000L,Ledger.available(s));assertEquals(16000L,Ledger.balance(s.accounts.first {it.id==id},s.entries));assertEquals(0L,Ledger.cashMonthly(s,YearMonth.from(today),EntryKind.INCOME))
    }
    @Test fun editedWithdrawalCannotBypassAvailableBalance() {
        val id=create(1000);db.investmentMovement(id,InvestmentAction.WITHDRAW,100,cash,today,"")
        val before=db.snapshot();assertThrows(Exception::class.java) {db.saveEntry(before.entries.single().copy(amount=1001))};assertEquals(before,db.snapshot())
    }
    @Test fun failedOperationsRollBackBothWallets() {
        val id=create(1000);val before=db.snapshot()
        assertThrows(Exception::class.java) {db.investmentMovement(id,InvestmentAction.WITHDRAW,1001,cash,today,"")}
        assertThrows(Exception::class.java) {db.investmentMovement(id,InvestmentAction.DEPOSIT,100001,cash,today,"")}
        assertThrows(Exception::class.java) {db.investmentMovement(id,InvestmentAction.LOSS,1001,null,today,"")}
        assertThrows(Exception::class.java) {db.investmentMovement(id,InvestmentAction.GAIN,1,null,today.plusDays(1),"")}
        assertEquals(before,db.snapshot())
    }
    @Test fun lockedFundsCannotBeSpentOrWithdrawnAndSalaryNeedsCash() {
        val id=create(10000,true)
        assertThrows(Exception::class.java) {db.investmentMovement(id,InvestmentAction.WITHDRAW,100,cash,today,"")}
        assertThrows(Exception::class.java) {db.saveEntry(Entry(title="Compra",amount=100,kind=EntryKind.EXPENSE,accountId=id,date=today,posted=true))}
        assertThrows(Exception::class.java) {db.addSeries(Series(title="Salário",amount=100,kind=EntryKind.INCOME,accountId=id,start=today,category="Salário"))}
        assertEquals(100000L,Ledger.available(db.snapshot()))
    }
    @Test fun metadataRatesAndHistoryRoundTripAndBadRestoreRollsBack() {
        val id=create();db.saveBenchmark(Benchmark("CDI","13.65",today,"2026-10-22T12:00:00Z"));db.investmentMovement(id,InvestmentAction.GAIN,100,null,today,"")
        val before=db.snapshot();val backup=db.exportJson();db.importJson(backup);assertEquals(before,db.snapshot())
        val broken=JSONObject(backup);broken.getJSONArray("investments").getJSONObject(0).put("rate","NaN")
        assertThrows(Exception::class.java) {db.importJson(broken.toString())};assertEquals(before,db.snapshot())
    }
    @Test fun restoreKeepsHistoricalValuationDate() {
        val id=create();db.investmentMovement(id,InvestmentAction.GAIN,500,null,today.minusDays(10),"")
        val backup=JSONObject(db.exportJson());backup.getJSONArray("investments").getJSONObject(0).put("valuation_date",today.minusDays(5).toString())
        db.importJson(backup.toString());assertEquals(today.minusDays(5),db.snapshot().investments.single().valuationDate)
    }
    @Test fun oldBackupReclassifiesInvestmentWithoutDuplicatingPrincipal() {
        val id=create(30000);db.investmentMovement(id,InvestmentAction.GAIN,500,null,today,"")
        val old=JSONObject(db.exportJson()).put("version",2);old.remove("investments");old.remove("benchmarks")
        db.importJson(old.toString());val s=db.snapshot()
        assertEquals(1,s.investments.size);assertEquals(id,s.investments.single().accountId)
        assertEquals(30500L,Ledger.balance(s.accounts.first {it.id==id},s.entries));assertEquals(100000L,Ledger.available(s));assertEquals("0",s.investments.single().rate)
    }
    @Test fun versionTwoDatabaseUpgradePreservesAccountIdsAndEntries() {
        val id=create(30000);db.investmentMovement(id,InvestmentAction.GAIN,500,null,today,"")
        db.writableDatabase.execSQL("DROP TABLE investments");db.writableDatabase.execSQL("DROP TABLE benchmarks");db.writableDatabase.version=2;db.close()
        db=FinanceDb(context,"invest-test.db") {today};val s=db.snapshot()
        assertEquals(3,db.readableDatabase.version);assertEquals(id,s.investments.single().accountId)
        assertEquals(30500L,Ledger.balance(s.accounts.first {it.id==id},s.entries));assertEquals(1,s.entries.size)
    }
    @Test fun deletionAndEditsResetProjectionBasisWithoutTouchingCash() {
        val id=create();val before=db.snapshot();db.investmentMovement(id,InvestmentAction.GAIN,100,null,today,"")
        db.deleteEntry(db.snapshot().entries.single().id);assertEquals(before,db.snapshot())
        assertThrows(Exception::class.java) {db.saveInvestment(before.accounts.first {it.id==id},before.investments.single().copy(valuationDate=today.plusDays(1)))}
    }
}
