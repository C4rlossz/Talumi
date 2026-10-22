package com.talumi.financas

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.pow

class InvestmentMathTest {
    private val month=YearMonth.of(2026,10)
    private val investment=Investment(2,rate="1",valuationDate=month.atDay(1).minusDays(1))
    @Test fun fullMonthlyRateAndPartialValuationDoNotInventPastGains() {
        assertEquals(100L,InvestmentMath.estimate(investment,10000,month,emptyList())!!.amount)
        val partial=InvestmentMath.estimate(investment.copy(valuationDate=month.atDay(15)),10000,month,emptyList())!!
        assertEquals(16,partial.calendarDays);assertTrue(partial.amount in 50..53)
        assertEquals(0L,InvestmentMath.estimate(investment.copy(valuationDate=month.atEndOfMonth()),10000,month,emptyList())!!.amount)
    }
    @Test fun annualUses252BusinessDaysAndHonorsHolidaysAndMaturity() {
        val base=investment.copy(mode=YieldMode.ANNUAL,rate="12",maturity=month.atDay(10),holidays=setOf(month.atDay(2)))
        val result=InvestmentMath.estimate(base,100000,month,emptyList())!!
        assertEquals(10,result.calendarDays);assertEquals(6,result.businessDays)
        assertEquals(Math.round(100000*((1.12).pow(6.0/252)-1)),result.amount)
    }
    @Test fun cdiUsesContractPercentageOnDailyRate() {
        val rate=Benchmark("CDI","13.65",month.atDay(1),"2026-10-01T00:00:00Z")
        val result=InvestmentMath.estimate(investment.copy(mode=YieldMode.CDI_PERCENT,rate="110"),100000000,month,listOf(rate))!!
        val expected=Math.round(100000000*((1+((1.1365).pow(1.0/252)-1)*1.1).pow(result.businessDays)-1))
        assertEquals(expected,result.amount)
        assertNull(InvestmentMath.estimate(investment.copy(mode=YieldMode.CDI_PERCENT),10000,month,emptyList()))
    }
    @Test fun selicCombinesAnnualSpreadAndClipsAtMaturity() {
        val rate=Benchmark("SELIC","13.65",month.atDay(1),"2026-10-01T00:00:00Z")
        val result=InvestmentMath.estimate(investment.copy(mode=YieldMode.SELIC_PLUS,rate="0.1"),100000,month,listOf(rate))!!
        assertEquals(Math.round(100000*((1.1365*1.001).pow(result.businessDays/252.0)-1)),result.amount)
        assertEquals(0L,InvestmentMath.estimate(investment.copy(maturity=month.atDay(1).minusDays(1)),10000,month,emptyList())!!.amount)
    }
    @Test fun strictRatesAndZeroBalance() {
        assertEquals("110.25",InvestmentMath.parseRate("110,2500"))
        listOf("NaN","Infinity","-1","1.12345","1001","1,2.3").forEach {value->assertThrows(Exception::class.java) {InvestmentMath.parseRate(value)}}
        assertEquals(0L,InvestmentMath.estimate(investment,0,month,emptyList())!!.amount)
    }
    @Test fun cashAndBankPieKeepInvestmentsSeparate() {
        val accounts=listOf(Account(1,"Conta",opening=50000),Account(2,"CDB A","Banco A",AccountKind.INVESTMENT,10000),Account(3,"CDB B","Banco A",AccountKind.INVESTMENT,20000))
        val state=FinanceState(accounts=accounts,loaded=true)
        assertEquals(50000L,Ledger.available(state))
        assertEquals(listOf("Banco A" to 30000L),InvestmentMath.groupedByBank(state))
        assertEquals(1,state.financeAccounts.size)
    }
}
