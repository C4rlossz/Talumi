package com.talumi.financas

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class LedgerTest {
    private val today=LocalDate.of(2026,10,21)
    private val bank=Account(1,"Banco",opening=10000)
    private val jar=Account(2,"Cofrinho",kind=AccountKind.JAR)
    private fun entry(id:Long=1,kind:EntryKind=EntryKind.EXPENSE,amount:Long=2500,posted:Boolean=true,date:LocalDate=today,to:Long?=null,debt:Long?=null)=Entry(id,"Teste",amount,kind,1,to,date,posted,debtId=debt)
    @Test fun expensesAndIncomeUseCents() {assertEquals(12501L,Ledger.balance(bank,listOf(entry(amount=2500),entry(2,EntryKind.INCOME,5001)),today))}
    @Test fun transferPreservesTotalAcrossOwnAccounts() {
        val moves=listOf(entry(kind=EntryKind.TRANSFER,amount=8000,to=2))
        assertEquals(2000L,Ledger.balance(bank,moves,today));assertEquals(8000L,Ledger.balance(jar,moves,today))
        assertEquals(10000L,listOf(bank,jar).sumOf {Ledger.balance(it,moves,today)})
        assertEquals(0L,Ledger.monthly(moves,YearMonth.from(today),EntryKind.EXPENSE))
    }
    @Test fun plannedDoesNotChangeActualBalance() {val moves=listOf(entry(posted=false));assertEquals(10000L,Ledger.balance(bank,moves,today));assertEquals(7500L,Ledger.balance(bank,moves,today,true))}
    @Test fun futureNotIncludedBeforeItsDate() {assertEquals(10000L,Ledger.balance(bank,listOf(entry(posted=false,date=today.plusDays(1))),today,true))}
    @Test fun negativeBalancesArePreserved() {assertEquals(-15000L,Ledger.balance(bank,listOf(entry(amount=25000)),today))}
    @Test fun monthlyTotalsExcludeOtherMonthsAndPlanned() {val moves=listOf(entry(amount=123),entry(2,amount=999,date=today.minusMonths(1)),entry(3,amount=999,posted=false));assertEquals(123L,Ledger.monthly(moves,YearMonth.from(today),EntryKind.EXPENSE))}
    @Test fun partialDebtSettlement() {val debt=Debt(1,"Ana","Empréstimo",10000,DebtKind.RECEIVABLE,today);assertEquals(7500L,Ledger.outstanding(debt,listOf(entry(kind=EntryKind.INCOME,debt=1))))}
    @Test fun leapYearAndMonthEndRetainAnchor() {val start=LocalDate.of(2024,1,31);assertEquals(LocalDate.of(2024,2,29),Ledger.occurrence(start,YearMonth.of(2024,2)));assertEquals(LocalDate.of(2024,3,31),Ledger.occurrence(start,YearMonth.of(2024,3)));assertEquals(LocalDate.of(2025,2,28),Ledger.occurrence(start,YearMonth.of(2025,2)))}
    @Test fun exactMoneyParsingAndEditing() {assertEquals(123456L,Money.parse("1234,56"));assertEquals(10L,Money.parse("0,10"));assertEquals(-25000L,Money.parse("-250",true));assertEquals("1234,56",Money.edit(123456))}
    @Test fun rejectAmbiguousOrInvalidMoney() {listOf("1.000","1.5","1,234","0","-1","abc","10000000000").forEach {assertThrows(IllegalArgumentException::class.java){Money.parse(it)}}}
    @Test fun strictDateRejectsImpossibleDay() {assertThrows(IllegalArgumentException::class.java){parseDate("31/02/2026")};assertEquals(LocalDate.of(2024,2,29),parseDate("29/02/2024"))}
    private fun salary(rule:PayRule=PayRule.FIFTH_BUSINESS_DAY,saturday:Boolean=false,holidays:Set<LocalDate> = emptySet())=Series(title="Salário",amount=100000,kind=EntryKind.INCOME,accountId=1,start=LocalDate.of(2026,5,1),category="Salário",rule=rule,day=5,autoPost=true,includeSaturday=saturday,holidays=holidays)
    @Test fun fifthBusinessDayExcludesWeekends() {assertEquals(LocalDate.of(2026,5,7),Ledger.occurrence(salary(),YearMonth.of(2026,5)))}
    @Test fun saturdayCanCountAsBusinessDay() {assertEquals(LocalDate.of(2026,5,6),Ledger.occurrence(salary(saturday=true),YearMonth.of(2026,5)))}
    @Test fun localHolidaysAreSkipped() {assertEquals(LocalDate.of(2026,5,8),Ledger.occurrence(salary(holidays=setOf(LocalDate.of(2026,5,1))),YearMonth.of(2026,5)))}
    @Test fun fifthBusinessDayHandlesYearAndMonthBoundaries() {assertEquals(LocalDate.of(2027,1,7),Ledger.occurrence(salary(),YearMonth.of(2027,1)))}
    @Test fun nextSalaryDoesNotBackfillBeforeStart() {val s=salary(PayRule.FIXED_DAY).copy(start=LocalDate.of(2026,10,21));assertEquals(LocalDate.of(2026,11,5),Ledger.nextOccurrence(s,s.start))}
    @Test fun fixedSalaryUsesLastDayInShortMonth() {assertEquals(LocalDate.of(2024,2,29),Ledger.occurrence(salary(PayRule.FIXED_DAY).copy(day=31),YearMonth.of(2024,2)))}
}
