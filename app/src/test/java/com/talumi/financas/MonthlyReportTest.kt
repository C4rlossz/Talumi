package com.talumi.financas

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MonthlyReportTest {
    private val today = LocalDate.of(2026,10,22)
    private val month = YearMonth.from(today)
    private val accounts = listOf(Account(1,"Banco",opening=10000),Account(2,"Cofrinho",kind=AccountKind.JAR,opening=2000),Account(3,"CDB",kind=AccountKind.INVESTMENT,opening=50000))
    private fun entry(amount:Long, kind:EntryKind=EntryKind.INCOME, account:Long=1, date:LocalDate=today, posted:Boolean=true, to:Long?=null, category:String="Outros") = Entry(title="Teste",amount=amount,kind=kind,accountId=account,date=date,posted=posted,toAccountId=to,category=category)
    private fun report(vararg entries:Entry, selected:YearMonth=month) = MonthlyReport.from(FinanceState(accounts=accounts,entries=entries.toList()),selected,today)

    @Test fun actualFlowsAndNetUseExactCents() {
        val result = report(entry(10001),entry(3002,EntryKind.EXPENSE))
        assertEquals(10001L,result.income);assertEquals(3002L,result.expense);assertEquals(6999L,result.net)
        assertEquals(18999L,result.balance);assertEquals(2,result.transactionCount)
        assertEquals(10001f/13003f,result.incomeShare,.00001f)
    }
    @Test fun scheduledFutureAndOtherMonthAreNotActualIncome() {
        val result = report(entry(999,posted=false),entry(888,date=today.plusDays(1)),entry(777,date=today.minusMonths(1)))
        assertEquals(0L,result.income);assertEquals(0,result.transactionCount)
        assertEquals(12777L,result.balance)
    }
    @Test fun investmentIncomeIsExcludedAndCashTransfersRetainAccounting() {
        val result = report(entry(900,account=3),entry(1000,EntryKind.TRANSFER,to=2),entry(4000,EntryKind.TRANSFER,to=3),entry(500,EntryKind.TRANSFER,account=3,to=1))
        assertEquals(0L,result.income);assertEquals(0L,result.expense);assertEquals(0,result.transactionCount)
        assertEquals(8500L,result.balance)
    }
    @Test fun expensesGroupedSortedWithFallbackAndNoTransferCategory() {
        val result = report(entry(200,EntryKind.EXPENSE,category="Transporte"),entry(100,EntryKind.EXPENSE,category="Alimentação"),entry(300,EntryKind.EXPENSE,category="Alimentação"),entry(50,EntryKind.EXPENSE,category=""),entry(999,EntryKind.TRANSFER,to=2,category="Transferência"))
        assertEquals(listOf(ExpenseCategory("Alimentação",400),ExpenseCategory("Transporte",200),ExpenseCategory("Outros",50)),result.categories)
        assertEquals(result.expense,result.categories.sumOf {it.amount})
    }
    @Test fun emptyAndSingleSideRingsHaveFiniteSharesAndNegativeNet() {
        assertEquals(0f,report().incomeShare,0f)
        assertEquals(1f,report(entry(1)).incomeShare,0f)
        val negative = report(entry(20000,EntryKind.EXPENSE))
        assertEquals(0f,negative.incomeShare,0f);assertEquals(-20000L,negative.net);assertEquals(-8000L,negative.balance)
    }
    @Test fun pastMonthStopsAtMonthEndAndFutureMonthHasNoActualFlow() {
        val september = month.minusMonths(1)
        val result = report(entry(999,date=september.atEndOfMonth()),entry(500),selected=september)
        assertEquals(999L,result.income);assertEquals(12999L,result.balance);assertEquals(september.atEndOfMonth(),result.through)
        val future = report(entry(500,date=month.plusMonths(1).atDay(1)),selected=month.plusMonths(1))
        assertEquals(0L,future.income);assertEquals(today,future.through)
    }
    @Test fun deletedOrUnknownAccountsDoNotEnterTheReport() {
        assertEquals(0L,report(entry(500,account=99)).income)
    }
    @Test fun largeAmountsDoNotOverflowIntWhenCalculatingSegments() {
        val result = report(entry(Money.MAX_CENTS),entry(Money.MAX_CENTS,EntryKind.EXPENSE))
        assertEquals(.5f,result.incomeShare,0f);assertEquals(0L,result.net)
    }
}
